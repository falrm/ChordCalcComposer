package com.jonlatane.composer.audio.generator;

/**
 * Created by jonlatane on 7/18/15.
 */
public class PitchShifter
{

    private static int MAX_FRAME_LENGTH = 16000;
    private static float[] gInFIFO = new float[MAX_FRAME_LENGTH];
    private static float[] gOutFIFO = new float[MAX_FRAME_LENGTH];
    private static float[] gFFTworksp = new float[2 * MAX_FRAME_LENGTH];
    private static float[] gLastPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private static float[] gSumPhase = new float[MAX_FRAME_LENGTH / 2 + 1];
    private static float[] gOutputAccum = new float[2 * MAX_FRAME_LENGTH];
    private static float[] gAnaFreq = new float[MAX_FRAME_LENGTH];
    private static float[] gAnaMagn = new float[MAX_FRAME_LENGTH];
    private static float[] gSynFreq = new float[MAX_FRAME_LENGTH];
    private static float[] gSynMagn = new float[MAX_FRAME_LENGTH];
    private static long gRover, gInit;

    public static void pitchShift(float pitchShift, long numSampsToProcess,
                                  float sampleRate, float[] indata) {
        pitchShift(pitchShift, numSampsToProcess, (long) 2048, (long) 10, sampleRate, indata);
    }

    public static void pitchShift(float pitchShift, long numSampsToProcess, long fftFrameSize,
                                  long osamp, float sampleRate, float[] indata) {
        double magn, phase, tmp, window, real, imag;
        double freqPerBin, expct;
        long i, k, qpd, index, inFifoLatency, stepSize, fftFrameSize2;


        float[] outdata = indata;
            /* set up some handy variables */
        fftFrameSize2 = fftFrameSize / 2;
        stepSize = fftFrameSize / osamp;
        freqPerBin = sampleRate / (double)fftFrameSize;
        expct = 2.0 * Math.PI * (double)stepSize / (double)fftFrameSize;
        inFifoLatency = fftFrameSize - stepSize;
        if (gRover == 0) gRover = inFifoLatency;


            /* main processing loop */
        for (i = 0; i < numSampsToProcess; i++)
        {

                /* As long as we have not yet collected enough data just read in */
            gInFIFO[(int)(gRover)] = indata[(int)(i)];
            outdata[(int)(i)] = gOutFIFO[(int)(gRover - inFifoLatency)];
            gRover++;

                /* now we have enough data for processing */
            if (gRover >= fftFrameSize)
            {
                gRover = inFifoLatency;

                    /* do windowing and re,im interleave */
                for (k = 0; k < fftFrameSize; k++)
                {
                    window = -.5 * Math.cos(2.0 * Math.PI * (double) k / (double) fftFrameSize) + .5;
                    gFFTworksp[(int)(2 * k)] = (float)(gInFIFO[(int)(k)] * window);
                    gFFTworksp[(int)(2 * k + 1)] = 0.0F;
                }


                    /* ***************** ANALYSIS ******************* */
                    /* do transform */
                shortTimeFourierTransform(gFFTworksp, fftFrameSize, -1);

                    /* this is the analysis step */
                for (k = 0; k <= fftFrameSize2; k++)
                {

                        /* de-interlace FFT buffer */
                    real = gFFTworksp[(int)(2 * k)];
                    imag = gFFTworksp[(int)(2 * k + 1)];

                        /* compute magnitude and phase */
                    magn = 2.0 * Math.sqrt(real * real + imag * imag);
                    phase = Math.atan2(imag, real);

                        /* compute phase difference */
                    tmp = phase - gLastPhase[(int)(k)];
                    gLastPhase[(int)(k)] = (float)phase;

                        /* subtract expected phase difference */
                    tmp -= (double)k * expct;

                        /* map delta phase into +/- Pi interval */
                    qpd = (long)(tmp / Math.PI);
                    if (qpd >= 0) qpd += qpd & 1;
                    else qpd -= qpd & 1;
                    tmp -= Math.PI * (double)qpd;

                        /* get deviation from bin frequency from the +/- Pi interval */
                    tmp = osamp * tmp / (2.0 * Math.PI);

                        /* compute the k-th partials' true frequency */
                    tmp = (double)k * freqPerBin + tmp * freqPerBin;

                        /* store magnitude and true frequency in analysis arrays */
                    gAnaMagn[(int)(k)] = (float)magn;
                    gAnaFreq[(int)(k)] = (float)tmp;

                }

                    /* ***************** PROCESSING ******************* */
                    /* this does the actual pitch shifting */
                for (int zero = 0; zero < fftFrameSize; zero++)
                {
                    gSynMagn[zero] = 0;
                    gSynFreq[zero] = 0;
                }

                for (k = 0; k <= fftFrameSize2; k++)
                {
                    index = (long)(k * pitchShift);
                    if (index <= fftFrameSize2)
                    {
                        gSynMagn[(int)(index)] += gAnaMagn[(int)(k)];
                        gSynFreq[(int)(index)] = gAnaFreq[(int)(k)] * pitchShift;
                    }
                }

                    /* ***************** SYNTHESIS ******************* */
                    /* this is the synthesis step */
                for (k = 0; k <= fftFrameSize2; k++)
                {

                        /* get magnitude and true frequency from synthesis arrays */
                    magn = gSynMagn[(int)(k)];
                    tmp = gSynFreq[(int)(k)];

                        /* subtract bin mid frequency */
                    tmp -= (double)k * freqPerBin;

                        /* get bin deviation from freq deviation */
                    tmp /= freqPerBin;

                        /* take osamp into account */
                    tmp = 2.0 * Math.PI * tmp / osamp;

                        /* add the overlap phase advance back in */
                    tmp += (double)k * expct;

                        /* accumulate delta phase to get bin phase */
                    gSumPhase[(int)(k)] += (float)tmp;
                    phase = gSumPhase[(int)(k)];

                        /* get real and imag part and re-interleave */
                    gFFTworksp[(int)(2 * k)] = (float)(magn * Math.cos(phase));
                    gFFTworksp[(int)(2 * k + 1)] = (float)(magn * Math.sin(phase));
                }

                    /* zero negative frequencies */
                for (k = fftFrameSize + 2; k < 2 * fftFrameSize; k++) gFFTworksp[(int)(k)] = 0.0F;

                    /* do inverse transform */
                shortTimeFourierTransform(gFFTworksp, fftFrameSize, 1);

                    /* do windowing and add to output accumulator */
                for (k = 0; k < fftFrameSize; k++)
                {
                    window = -.5 * Math.cos(2.0 * Math.PI * (double) k / (double) fftFrameSize) + .5;
                    gOutputAccum[(int)(k)] += (float)(2.0 * window * gFFTworksp[(int)(2 * k)] / (fftFrameSize2 * osamp));
                }
                for (k = 0; k < stepSize; k++) gOutFIFO[(int)(k)] = gOutputAccum[(int)(k)];

                    /* shift accumulator */
                //memmove(gOutputAccum, gOutputAccum + stepSize, fftFrameSize * sizeof(float));
                for (k = 0; k < fftFrameSize; k++)
                {
                    gOutputAccum[(int)(k)] = gOutputAccum[(int)(k + stepSize)];
                }

                    /* move input FIFO */
                for (k = 0; k < inFifoLatency; k++) gInFIFO[(int)(k)] = gInFIFO[(int)(k + stepSize)];
            }
        }
    }

    private static void shortTimeFourierTransform(float[] fftBuffer, long fftFrameSize, long sign) {
        float wr, wi, arg, temp;
        float tr, ti, ur, ui;
        long i, bitm, j, le, le2, k;

        for (i = 2; i < 2 * fftFrameSize - 2; i += 2)
        {
            for (bitm = 2, j = 0; bitm < 2 * fftFrameSize; bitm <<= 1)
            {
                if ((i & bitm) != 0) j++;
                j <<= 1;
            }
            if (i < j)
            {
                temp = fftBuffer[(int)(i)];
                fftBuffer[(int)(i)] = fftBuffer[(int)(j)];
                fftBuffer[(int)(j)] = temp;
                temp = fftBuffer[(int)(i + 1)];
                fftBuffer[(int)(i + 1)] = fftBuffer[(int)(j + 1)];
                fftBuffer[(int)(j + 1)] = temp;
            }
        }
        long max = (long)(Math.log(fftFrameSize) / Math.log(2.0) + .5);
        for (k = 0, le = 2; k < max; k++)
        {
            le <<= 1;
            le2 = le >> 1;
            ur = 1.0F;
            ui = 0.0F;
            arg = (float)Math.PI / (le2 >> 1);
            wr = (float)Math.cos(arg);
            wi = (float)(sign * Math.sin(arg));
            for (j = 0; j < le2; j += 2)
            {

                for (i = j; i < 2 * fftFrameSize; i += le)
                {
                    tr = fftBuffer[(int)(i + le2)] * ur - fftBuffer[(int)(i + le2 + 1)] * ui;
                    ti = fftBuffer[(int)(i + le2)] * ui + fftBuffer[(int)(i + le2 + 1)] * ur;
                    fftBuffer[(int)(i + le2)] = fftBuffer[(int)i] - tr;
                    fftBuffer[(int)(i + le2 + 1)] = fftBuffer[(int)(i + 1)] - ti;
                    fftBuffer[(int)(i)] += tr;
                    fftBuffer[(int)(i + 1)] += ti;

                }
                tr = ur * wr - ui * wi;
                ui = ur * wi + ui * wr;
                ur = tr;
            }
        }
    }
}
