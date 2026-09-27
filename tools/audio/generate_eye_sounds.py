"""
Synthesizes 10 custom, commercial-use safe, authentic organic audio effects for Eye of Cthulhu
and encodes them to 44.1kHz OGG Vorbis for Minecraft.
"""

import os
import subprocess
import numpy as np
import scipy.io.wavfile as wavfile
from pathlib import Path

SAMPLE_RATE = 44100
TARGET_DIR = Path("src/main/resources/assets/terraforge_rpg/sounds/boss/eye_of_cthulhu")
TEMP_DIR = Path("build/temp_audio")

TARGET_DIR.mkdir(parents=True, exist_ok=True)
TEMP_DIR.mkdir(parents=True, exist_ok=True)

def normalize(audio):
    peak = np.max(np.abs(audio))
    if peak > 1e-6:
        return audio / peak * 0.95
    return audio

def save_and_convert(name, audio):
    audio_int16 = (normalize(audio) * 32767).astype(np.int16)
    wav_path = TEMP_DIR / f"{name}.wav"
    ogg_path = TARGET_DIR / f"{name}.ogg"
    
    wavfile.write(str(wav_path), SAMPLE_RATE, audio_int16)
    
    cmd = [
        "ffmpeg", "-y", "-i", str(wav_path),
        "-c:a", "libvorbis", "-q:a", "6",
        str(ogg_path)
    ]
    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    print(f"Generated {ogg_path} ({ogg_path.stat().st_size} bytes)")

def synth_ambient():
    duration = 3.5
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Deep rhythmic pulsing (heartbeat/pulse of massive ocular organ)
    pulse = np.sin(2 * np.pi * 1.2 * t) ** 4
    rumble = np.sin(2 * np.pi * 55 * t + np.sin(2 * np.pi * 3 * t)) * pulse
    # Wet squishing high frequencies
    noise = np.random.normal(0, 0.15, len(t))
    wet = noise * (np.sin(2 * np.pi * 2.4 * t) ** 6)
    # Filter wet sound
    b = np.convolve(wet, np.hanning(150)/np.sum(np.hanning(150)), mode='same')
    # Tentacle flutter
    flutter = np.sin(2 * np.pi * 120 * t + np.sin(2 * np.pi * 8 * t)) * 0.1
    env = np.sin(np.pi * t / duration) ** 0.5
    return (rumble * 0.7 + b * 0.5 + flutter * 0.3) * env

def synth_roar():
    duration = 2.2
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Pitch envelope: swoops up then guttural drop
    f_env = 180 + 350 * np.exp(-1.5 * t) - 60 * t
    phase = 2 * np.pi * np.cumsum(f_env) / SAMPLE_RATE
    # Harmonics + FM distortion
    fm = np.sin(2 * np.pi * 75 * t) * 3.0
    voice = np.sin(phase + fm) + 0.6 * np.sin(2 * phase) + 0.4 * np.sin(3 * phase)
    # Guttural rasp noise
    noise = np.random.normal(0, 0.4, len(t))
    rasp = voice * noise
    env = np.minimum(t * 8.0, 1.0) * np.exp(-1.2 * t)
    return (voice * 0.6 + rasp * 0.6) * env

def synth_prepare():
    duration = 1.0
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Inhaling tension / high-pitch spin-up
    f_env = 80 + 400 * (t / duration) ** 2
    phase = 2 * np.pi * np.cumsum(f_env) / SAMPLE_RATE
    spin = np.sin(phase) * (t / duration)
    # Muscular contraction tension
    groan = np.sin(2 * np.pi * (60 + 50 * t) * t) * (1.0 - t / duration)
    # Air sucking whoosh
    noise = np.random.normal(0, 0.25, len(t)) * (t / duration) ** 1.5
    smooth_noise = np.convolve(noise, np.hanning(100)/np.sum(np.hanning(100)), mode='same')
    return (spin * 0.5 + groan * 0.4 + smooth_noise * 0.6)

def synth_charge():
    duration = 1.4
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Massive air displacement whoosh + doppler drop
    noise = np.random.normal(0, 0.5, len(t))
    env_whoosh = np.exp(-2.5 * (t - 0.2) ** 2)
    whoosh = np.convolve(noise, np.hanning(80)/np.sum(np.hanning(80)), mode='same') * env_whoosh
    # Trailing roar
    f_env = 320 * np.exp(-2.0 * t) + 70
    phase = 2 * np.pi * np.cumsum(f_env) / SAMPLE_RATE
    growl = np.sin(phase + np.sin(2 * np.pi * 45 * t) * 2.0) * np.exp(-1.5 * t)
    return whoosh * 0.7 + growl * 0.6

def synth_bite():
    duration = 0.7
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Heavy visceral jaw snap (sharp transient click + bone crunch + wet tear)
    snap_t = np.exp(-60.0 * t) * np.sin(2 * np.pi * 850 * t)
    # Bone crunch (burst of chaotic noise)
    crunch = np.random.normal(0, 0.6, len(t)) * np.exp(-25.0 * t)
    crunch_filt = np.convolve(crunch, np.hanning(40)/np.sum(np.hanning(40)), mode='same')
    # Deep jaw impact thud
    thud = np.sin(2 * np.pi * 90 * np.exp(-15.0 * t) * t) * np.exp(-10.0 * t)
    # Wet squish after-bite
    squish = np.random.normal(0, 0.3, len(t)) * np.sin(2 * np.pi * 350 * t) * np.exp(-8.0 * (t - 0.05) ** 2)
    return snap_t * 0.8 + crunch_filt * 0.7 + thud * 0.9 + squish * 0.4

def synth_hurt():
    duration = 0.6
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Visceral squishy impact grunt
    f_env = 240 * np.exp(-10.0 * t) + 90
    phase = 2 * np.pi * np.cumsum(f_env) / SAMPLE_RATE
    grunt = np.sin(phase) * np.exp(-6.0 * t)
    splat = np.random.normal(0, 0.5, len(t)) * np.exp(-18.0 * t)
    splat_filt = np.convolve(splat, np.hanning(50)/np.sum(np.hanning(50)), mode='same')
    return grunt * 0.6 + splat_filt * 0.7

def synth_transition():
    duration = 3.2
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Convulsive tearing of flesh, grotesque rupture
    # Slower tearing rhythms
    tear_noise = np.random.normal(0, 0.4, len(t))
    rhythm = (np.sin(2 * np.pi * 4.0 * t) ** 4)
    tear = tear_noise * rhythm * (1.0 + t / duration)
    tear_filt = np.convolve(tear, np.hanning(60)/np.sum(np.hanning(60)), mode='same')
    # Build up to massive rupture crack at t = 2.0s
    crack_t = np.maximum(0, t - 2.0)
    snap = np.exp(-40.0 * crack_t) * np.sin(2 * np.pi * 450 * crack_t) * (t >= 2.0)
    # Agonized shriek rising and distorting
    shriek_f = 120 + 280 * (t / duration)
    shriek = np.sin(2 * np.pi * shriek_f * t + np.sin(2 * np.pi * 80 * t) * 3.0) * (t / duration) ** 2
    env = np.exp(-0.5 * (t - 2.0) ** 2) if t[-1] > 2.0 else 1.0
    return tear_filt * 0.5 + snap * 0.9 + shriek * 0.6

def synth_phase2_roar():
    duration = 2.8
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Extremely aggressive, hollow maw reverberation, dual vocal tracts
    f1 = 260 * np.exp(-1.0 * t) + 80
    f2 = 140 * np.exp(-0.8 * t) + 50
    phase1 = 2 * np.pi * np.cumsum(f1) / SAMPLE_RATE
    phase2 = 2 * np.pi * np.cumsum(f2) / SAMPLE_RATE
    # Metallic/chitinous resonance from razor teeth
    teeth_chatter = np.sin(2 * np.pi * 480 * t + np.sin(2 * np.pi * 18 * t) * 4.0) * 0.35
    maw = (np.sin(phase1) + 0.8 * np.sin(phase2)) * (1.0 + np.sin(2 * np.pi * 60 * t) * 0.5)
    noise = np.random.normal(0, 0.35, len(t)) * (np.sin(phase1) ** 2)
    env = np.minimum(t * 10.0, 1.0) * np.exp(-0.8 * t)
    return (maw * 0.7 + teeth_chatter * 0.4 + noise * 0.5) * env

def synth_death():
    duration = 3.6
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Final deflating death screech and collapse into gory ruin
    screech_f = 400 * np.exp(-2.5 * t) + 40
    phase = 2 * np.pi * np.cumsum(screech_f) / SAMPLE_RATE
    screech = np.sin(phase + np.sin(2 * np.pi * 30 * t) * 2.0) * np.exp(-1.0 * t)
    # Squelchy collapse and wet splattering
    splat_noise = np.random.normal(0, 0.4, len(t)) * np.exp(-0.8 * t)
    splat_filt = np.convolve(splat_noise, np.hanning(120)/np.sum(np.hanning(120)), mode='same')
    # Deep sub-bass death quake
    sub = np.sin(2 * np.pi * 38 * t) * np.exp(-0.7 * t)
    return screech * 0.5 + splat_filt * 0.6 + sub * 0.7

def synth_servant_summon():
    duration = 0.9
    t = np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)
    # Wet organic ejection / birth pop
    pop = np.exp(-35.0 * t) * np.sin(2 * np.pi * 320 * np.exp(-12.0 * t) * t)
    # Squishy ejection flutter
    flutter = np.sin(2 * np.pi * 180 * t) * np.exp(-8.0 * t) * (np.sin(2 * np.pi * 25 * t) ** 2)
    noise = np.random.normal(0, 0.3, len(t)) * np.exp(-12.0 * t)
    noise_filt = np.convolve(noise, np.hanning(60)/np.sum(np.hanning(60)), mode='same')
    return pop * 0.8 + flutter * 0.5 + noise_filt * 0.6

def main():
    sounds = {
        "ambient": synth_ambient,
        "roar": synth_roar,
        "prepare": synth_prepare,
        "charge": synth_charge,
        "bite": synth_bite,
        "hurt": synth_hurt,
        "transition": synth_transition,
        "phase2_roar": synth_phase2_roar,
        "death": synth_death,
        "servant_summon": synth_servant_summon
    }
    
    for name, func in sounds.items():
        audio = func()
        save_and_convert(name, audio)

if __name__ == "__main__":
    main()
