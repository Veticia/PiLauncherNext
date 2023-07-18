# Latest version: [piLauncherNext_v117+1.1.7.apk](https://github.com/JarJarBlinkz/PiLauncherNext/releases/download/v117/PiLauncherNext.v117+1.1.7.apk))

## Changelog

### 1.1.7
Added:
- VRPlayer_Shortcut which gives you the ability to add a vr animation player pinned to taskbar to use as a shortcut to open and close the launcher.
- Added a webview for Coming Soon & Pre-Orders in the settings screen as a quick reference guide of new content coming to quest & auto updates every 7 days
- Fixed some typos
- Updated some language packs
- Removed ability to use explore icon as shortcut from settings (did not always work)
- Made all icons uniform and correctly spaced
- Added games and media tabs to main screen
- Removed settings shortcut from top right of main window as pressing B button on controller opens settings window
- Icons can be moved to specific tabs to organise your library



-- Previous additions from original fork below --

### 1.1.1
- (After consulting my lawyers) Reverted "download icons directly from Pico store" to not make Pico angry at me

### 1.1.0
- More reliable updates checker
- Icons spacing with names turned on now pixel perfect
- Added File Manager icon to PicoOS 4
- More accesible settings on smaller window sizes / dpi (Pico 3, Oculus)
- Option to download icons directly from Pico store (Pico only; based on logic from [PicoZen](https://github.com/barnabwhy/PicoZen))

### 1.0.4
- Reduced stutter when switching categories
- Modernized UI by replacing checkboxes with switches

### 1.0.3
- Fixed crash when removing last group (removing last group will restore default groups)
- Added icon for Mixed Reality Capture (Pico only)
- Restored System Tweaks menu (Oculus only)

### 1.0.2
- hotfix: default group PSP not created on first launch

### 1.0.1
- hotfix: nullPointerException crash on first run

### 1.0.0
- Sorting options (by name, recently launched, install date) [@Maarten van Dillen](https://github.com/maartenvandillen/)
- Greatly improved UI performance
- Improved icons loading (fixed broken icons bug, faster PSP icon)
- Improved custom icons quality
- Adjusted translations (thanks habhabi for help with German and Italian)
- Minor code improvements

### 0.9.0
- Improved performance (faster icon generation and loading, less stuttering while scrolling)
- Automatic categories (android apps and other tools in separate category by default)
- Not displaying apps that cannot be launched anyway
- Loading higher quality icons (if we don't have them in our repo)
- Improved Launcher Settings layout
- Improved Explore replacement
- Adjusted german translation
- Removed Tweaks menu (Oculus only)
- Fixed bug that allowed Launcher settings to be opened twice
- Fixed memory leak... twice
- Fixed possible crash if launched without categories
- Many other minor code improvements

### 0.8.5
- Fixed possible crash when changing icon

### 0.8.4
- Added autorun option (enabled by default)
- More consistent and grammatically correct Russian translation [@Levkonlev](https://github.com/Levkonlev)
- Adjusted wording in other languages

### 0.8.3
- Added Streaming Assistant (Pico)
- Removed some common app plugins (that we cannot run anyway)
- Sexy spinner

### 0.8.2
- Fixed fuckup that made PSP games not work

### 0.8.1
- Added support for Chinese and Thai.
- Made icons larger and reduced whitespace to make the interface more compact (especially with labels turned off).
- Fine-tuned the scale slider to make it more granular and made lower values more useful on Pico.
- UI bug fix: Clicking on background themes no longer changes the theme icon opacity.
- Small changes and code cleanup.

### 0.8.0
- Updater
- Scalable text under icons
- Changeable icon styles. Two variant's based on Pico store and Tenaldo's (thanks for help).
- Image picker no longer a big bucket
- Unhid Pico Browser and Pico Video
- Upscaled Android apps icons

### 0.7.0
Initial release of piLauncherNext. Compared to original piLauncher:
- Dropped support for Meta Quest (it may work, I just don't support it)
- Added support for all languages found in Pico except for Thai and Chinese
- Added button in settings for easy replacement of Explore app
- Higher quality icons
- Fixed bug where some icons werent displayed
- Fixed bug where no icons were displayed in Turkish
