# Features

Filmory is a Bookmory clone for movies and series. This file is the source of truth for product scope. The clickable mockup lives in the "Filmory Mockup" artifact (https://claude.ai/artifact/4eAcUCNPXCPsExChZ7ZdiK). Where this file and the mockup disagree, this file wins.

## In scope

### Titles, statuses, wishlist and watch later

- A title is a movie or a series. Both are called "titles" below.
- A title has one **status**: Not watched, Watching (series only), Watched, On hold or Dropped. These match Bookmory's To read, Reading, Finished, Paused and Gave up.
- **Wishlist** is a separate flag on a title (the heart button), not a status. A wishlisted title can have any status. The Wishlist list on Home shows all flagged titles. The library setting "Include wishlist titles" controls whether flagged titles show in the library.
- **Watch later** is another separate flag. Flagged titles fill the slots of the Watch later row on Home, in a user-chosen order.
- Movies have no progress tracking. A movie is Not watched, Watched, On hold or Dropped.
- Series also track the episode you are on (see "Series progress").
- The Home lists (Continue watching, On hold, Dropped) and the Watched tab are built from the title status, with the one exception in the next section.

### Watchings (rewatches)

- A title can have several separate **watchings**. Each watching has its own start and finish dates, rating, review, state and notes.
- A watching has a state: In progress, On hold, Dropped or Finished.
- The title status follows these rules:
  - Until a watching has been finished, the title status mirrors its latest watching (start sets Watching for series, finish sets Watched, and so on). The user can also set a movie's status directly.
  - Once any watching is finished, the title stays **Watched**. Dropping or pausing a later rewatch changes only that watching, not the title.
  - While a rewatch is in progress on a Watched title, the title also appears under Continue watching on Home. It still counts as Watched everywhere else.
  - The user can always change the title status by hand.
- The user can see the notes of every watching together in one view. The title detail screen has watching chips that can be selected together, and "All watchings" clears the selection. The Notes tab lists notes from all watchings of all titles.

### Series progress

- Each watching of a series has a current-episode pointer (season and episode) with a quick "next episode" action. Advancing the pointer checks the current episode.
- Episodes can also be checked off individually, for people who skip around. Each check stores the date and time it was made, so the calendar can show episodes watched.
- An episode has three states: not watched, watched and skipped. Tapping its circle cycles through them in that order. Skipped is for episodes left out on purpose, such as filler. It counts as checked, so a series can reach 100% without them, but it is not watched: the calendar ignores it. The progress card shows a two-colour bar (watched, then skipped) with the number of episodes checked, watched and skipped. Mark season checks only the episodes that have no state and leaves skipped ones skipped.
- Tapping the "You are here" badge opens a sheet with the same Series card as Profile.
- By default ticking an episode does not move the "You are here" marker. The Series card in Profile lets the user make the marker follow ticks (setting "Marker moves"): it lands on the next episode or on the ticked one, and ticking an episode before the marker moves it only if "Also when ticking earlier episodes" is on. Unticking never moves it, and Mark season behaves like ticking the season's last episode. The card shows a live example of the chosen rule.
- Seasons are shown as expandable lists of episodes with a progress bar.
- When a series is added by hand, the user enters the number of seasons and the number of episodes in each season (episode titles are optional). When it is added from TMDB, seasons and episodes are filled in automatically.

### Notes ("thoughts")

- Notes can be added to a movie, to a series as a whole, and to each episode. Notes are never required.
- A note belongs to a watching. If the user adds a note to a title that has no watching yet (for example a wishlisted title), a watching is created automatically with no start date.
- Note types: **Scene**, **Summary**, **Thought**, **Question**.
- A note can carry an optional timestamp into the movie or episode (h:mm:ss). Notes on series can also name the episode.
- A note can have a date and time, an attached image, and can be marked favorite or copied.
- Notes can be edited and deleted from the note detail screen.
- A note can have comments, written by the user on their own notes. Comments are not shown under the note in lists. They are shown on the note detail screen, which opens when the note is tapped. A note in a list only shows a comment count. Comments can be added and deleted there.
- The screen for adding or editing a note shows the title's poster large at the top.
- The Notes tab (Bookmory's Memorize) shows all notes with random note and favorite notes shortcuts, a type filter and sorting.

### Formatted text

- Note text, note comments, the watching review and the title description are written in a small Markdown subset: `**bold**`, `*italic*`, `~~strikethrough~~`, `# ` and `## ` headings, `> ` quotes (for things said in the movie or series) and `- ` bullets. Underline is not supported, because Markdown has no underline.
- These fields are small text fields that show the formatting as you type, like Obsidian's live preview: the markers of a run (and the `#`, `>` or `-` of a line) are hidden, and show dimmed only while the cursor or selection is in or next to that run or line. A button in the corner opens a full-screen editor with a toolbar above the keyboard. Everywhere the text is shown, the markers are hidden and the formatting applied. Copying a note copies the raw Markdown.

### Rating and review

- Rating is 0 to 5 stars in half steps. Stars can be set by tapping or by dragging a finger across them.
- Rating and review belong to a watching. A title card shows the rating of its latest watching.

### Collections

- Movies and series (not episodes) can be organized into collections. There is a default "Favorites" collection. It can be renamed but not deleted.
- A collection has its own screen: the title list under a header with an editable name. Long-pressing the name edits it in place, a short tap says to press and hold. A new collection (Add on the Collections screen) is created at once as "New collection" (numbered if taken) and opens with its name selected and the keyboard up.
- A collection can have an icon (one of 25 drawn symbols, an emoji or a photo) and a color (13 presets or any color). Text on the card switches between white and black so it stays readable. Tapping the icon in the header opens the icon and color sheet.
- Collections have one global order and can be pinned. Pinned collections come first everywhere (Collections screen, Home row, add-to-collection sheet). In the add-to-collection sheet, Edit switches to management mode: drag to reorder, pin, rename, delete (with a confirm) and add.

### Calendar

- Calendar has the same settings as Bookmory, applied to movies and series.
- Titles appear on finish dates, as a start to finish range, and on logged dates (notes, episodes checked, start and end dates). The user can turn each of the three on or off.
- Other settings: stars on the finish date, vertical border, completed/dropped badge, grid or stack layout when several titles fall on one day, and which day the week starts on.
- The calendar can be exported.

### Metadata

- Titles can be found with a search against TMDB (posters, details and full episode lists for series), or added by hand. Manual entry works with no network.
- TMDB requires attribution. Show the TMDB logo and the required notice on the search screen and in the settings/about screen.

### Backup and restore, and sync

- The app is local on the device.
- Backup and restore export and import the entire app state as a zip file with the extension `.filmorybkp`. "Save to device" opens the system save-as screen (suggested name `filmory-YYYY-MM-DD.filmorybkp`) so the user picks the folder; "Share" hands the file to another app.
- Sync is not built now. The data layer must stay simple to extend with a web server later.

### Home screen

- Header with greeting and a "Continue watching" card for the series in progress. With several titles the cards swipe sideways and the next card peeks in at the right edge. The header color follows the poster of the card in view, mixed with the theme blue and darkened as far as needed to keep the white text readable. A setting in Profile turns the tint off.
- Watch later row, Wishlist and Calendar cards, Collections row, On hold and Dropped cards, and a Library card.
- Purchase, gift and loan records from Bookmory are not part of Filmory.
- The layout is editable. Everything below the header is one ordered list of blocks: Watch later, Collections and the tiles Wishlist, Calendar, On hold, Dropped and Library. The "Edit layout" button in the app bar turns on edit mode: drag any block to a new place (sections by their handle, tiles by long press), or use the Move up and Move down accessibility actions. Done or the system Back button leaves edit mode and "Reset" restores the default order. Sections use the full width, tiles sit in two columns, and a tile with no partner in its row takes the full width. The order is stored in settings. Hiding blocks is not supported.

### Library

- Grid or list of all titles, with filters for status and type (movie or series), sorting, and display settings (layout, show stars, show status icon, include wishlist titles).

### Watched tab

- Bookmory's Achievement tab. A grid of finished titles sorted by finish date, filterable by movies and series.

## Out of scope for now

- Streaks.
- Statistics (daily, annual and tag statistics).
- Rewind.
- Premium features.
- Purchase, gift and loan records.

## Look and feel

- The look follows Bookmory closely: cream background, solid blue header with a deep curve, soft shadowed cards, section titles with a highlighter underline, pill-shaped chips, and a rounded bottom bar with a circular active item.
- Colors: the Bookmory-like cream and blue palette is the default on every device. A setting turns on Material You dynamic color, available on Android 12 and up only. Light and dark theme.
- Built with Material 3 components.
- Bottom bar with four tabs: Home, Notes, Watched, Profile.
- Watch later slots are squircles (rounded squares), not circles or plain rectangles.
- The On hold icon is a bold filled pause symbol.
- Keep Bookmory's nice animations: staggered card entrance, growing progress bars, animated navigation indicator, pop-in for checks and the floating action button. Animations must respect the system reduced-motion setting.

## Not designed yet

These screens have no mockup. The implementing agent should build them in the same style, following Bookmory's equivalent screens:

- Profile and Settings (theme and dynamic color switch, backup and restore, TMDB attribution).
- The list screens: Wishlist, a collection, On hold, Dropped, and the collection list with its "add collection" tile.
- The TMDB search and add flow, and the manual add and edit form (including entering seasons and episodes).

Bookmory's "Series" feature (grouping books of one series) has no equivalent here. Franchise grouping is covered by collections.
