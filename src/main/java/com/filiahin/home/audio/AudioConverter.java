package com.filiahin.home.audio;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

public class AudioConverter {
    private static final String TARGET = "../../home.ogg";
    public AudioMessage convert(String sourcePath) {
        File target = new File(TARGET);
        File source = new File(sourcePath);

        try {
            MultimediaObject multimedia = new MultimediaObject(source);

            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libvorbis");
            audio.setBitRate(128000);
            audio.setChannels(2);
            audio.setSamplingRate(44100);

            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("ogg");
            attrs.setAudioAttributes(audio);

            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(multimedia, target, attrs);

            AudioFile audioFile = AudioFileIO.read(target);
            long durationInSeconds = audioFile.getAudioHeader().getTrackLength();

            return new AudioMessage(target, durationInSeconds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
