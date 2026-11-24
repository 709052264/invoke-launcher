<div align="center">
  <img src="docs/logo.svg" alt="Invoke Launcher Logo" width="128" height="128" />
  <h1>Invoke</h1>
  <p><strong>A fast, fluid Material 3 Android launcher built with Jetpack Compose.</strong></p>
  <p>
    <a href="#screenshots">Screenshots</a> &middot;
    <a href="#quick-start">Quick Start</a> &middot;
    <a href="#features">Features</a> &middot;
    <a href="#gestures">Gestures</a> &middot;
    <a href="#privacy">Privacy</a> &middot;
    <a href="#sponsor">Sponsor</a>
  </p>
</div>

---

## Screenshots

<table>
  <tr>
    <td align="center" width="25%">
      <img src="docs/screenshots/home-canvas.png" alt="Everyday Home Canvas" width="220" /><br />
      <sub><strong>Everyday Home</strong><br />Bento tiles, widgets &amp; clock</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/invoke-search.png" alt="Invoke App Drawer & Search" width="220" /><br />
      <sub><strong>App Drawer &amp; Search</strong><br />Fuzzy search &amp; A&ndash;Z rail</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/home-editor.png" alt="Home Canvas Editor" width="220" /><br />
      <sub><strong>Home Editor</strong><br />13 morphing shapes &amp; inspector</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/widget-board.png" alt="Add Widget Board" width="220" /><br />
      <sub><strong>Add Widget</strong><br />App-grouped widgets</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="25%">
      <img src="docs/screenshots/customization-studio.png" alt="Customization Studio — Color & Palettes" width="220" /><br />
      <sub><strong>Color &amp; Palettes</strong><br />Material You &amp; HSV picker</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/studio-clock.png" alt="Customization Studio — Clock & Typography" width="220" /><br />
      <sub><strong>Clock &amp; Fonts</strong><br />Faces, details &amp; font pairings</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/studio-drawer-theme.png" alt="Customization Studio — Drawer, OLED & Motion" width="220" /><br />
      <sub><strong>Drawer &amp; OLED</strong><br />Dock, blur &amp; motion tuning</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/night-oled.png" alt="Night OLED Mode" width="220" /><br />
      <sub><strong>Night OLED</strong><br />Pure black canvas</sub>
    </td>
  </tr>
</table>

---

## Quick Start

1. **Set Default**: Open **Settings &rarr; Apps &rarr; Default apps &rarr; Home app** and pick **Invoke**.
2. **Search**: Swipe up anywhere on the home screen or tap the **Invoke** dock pill.
3. **Customize**: Tap the bottom-right button to edit tiles, add widgets, or open **Studio**.
4. **Switch Modes**: Toggle between **Everyday**, **Deep Focus** (top 4 essentials), and **Night OLED** (pure black).

---

## Features

- **Bento Canvas** &mdash; Mix apps, widgets, quick people, and shortcuts into flexible 1x1, 2x1, or 2x2 blocks.
- **13 Morphing Shapes** &mdash; Squircles, leaves, arches, pills, clovers, and gems with smooth touch morphing.
- **Universal Search** &mdash; Search apps by name or initials (yt for YouTube), run math in the search bar, or dial contacts with natural verbs (call Mom).
- **Workspace Modes** &mdash; Everyday, Deep Focus, and true-black Night OLED (#000000).
- **Customization Studio** &mdash; Dynamic wallpaper colors, 4 curated palettes, 2D color picker, clock styles, font pairings, and drawer transparency.

---

## Gestures

| Gesture | Action |
| :--- | :--- |
| **Swipe Up** | Opens App Drawer &amp; Search |
| **Swipe Down** | Opens Status Bar / Notifications |
| **Tap Bottom-Right Button** | Opens Mode Menu (Edit, Widgets, Studio, Modes) |
| **Long-Press Tile** | Shortcuts, resize, shape, and tile settings |
| **Scrub Right Edge** | Alphabet index jump in drawer |

---

## Sponsor

Invoke is independent, open source, and ad-free.

<p>
  <a href="http://invokevirtual.org/sponsor/">
    <img src="docs/sponsor.svg" alt="Sponsor Invoke" height="48" />
  </a>
</p>

---

## Installing

Download the latest APK from GitHub Releases or build from source:

```bash
git clone https://github.com/invokevirtual/invoke.git
cd invoke
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. Requires Android 8.0+.

---

## License

Apache License 2.0. See `LICENSE` for details.
