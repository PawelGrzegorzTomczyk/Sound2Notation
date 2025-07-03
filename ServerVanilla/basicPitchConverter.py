from basic_pitch.inference import predict
from basic_pitch import ICASSP_2022_MODEL_PATH
from music21 import midi, converter
import os

def process(path, path_to_midi, path_to_xml):
    model_output, midi_data, note_events = predict(
        audio_path=path,
        model_or_model_path=ICASSP_2022_MODEL_PATH,
        onset_threshold=0.85,
        frame_threshold=0.60,
        minimum_note_length=200.00,
        minimum_frequency=50.0,
        maximum_frequency=2000.0,
        multiple_pitch_bends=False,
        melodia_trick=True,
        debug_file=None,
        midi_tempo=120
    )

    midi_data.write(path_to_midi)
    score = converter.parse(path_to_midi)
    score.write('musicxml', fp=path_to_xml)
    if os.path.exists(path_to_midi):
        os.remove(path_to_midi)