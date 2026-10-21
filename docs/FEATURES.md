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
- The user can see the notes of every watching together in one view. The title detail screen has an "All watchings" filter, and the Notes tab lists notes from all watchings of all titles.

### Series progress

- Each watching of a series has a current-episode pointer (season and episode) with a quick "next episode" action. Advancing the pointer checks the current episode.
- Episodes can also be checked off individually, for people who skip around. Each check stores the date and time it was made, so the calendar can show episodes watched.
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

### Rating and review

- Rating is 0 to 5 stars in half steps.
- Rating and review belong to a watching. A title card shows the rating of its latest watching.

### Collections

- Movies and series (not episodes) can be organized into collections. There is a default "Favorites" collection.

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
- Backup and restore export and import the entire app state as a zip file with the extension `.filmorybkp`.
- Sync is not built now. The data layer must stay simple to extend with a web server later.

### Home screen

- Header with greeting and a "Continue watching" card for the series in progress.
- Watch later row, Wishlist and Calendar cards, Collections row, On hold and Dropped cards, and a Library card.
- Purchase, gift and loan records from Bookmory are not part of Filmory.

### Library

- Grid or list of all titles, with filters for status and type (movie or series), sorting, and display settings (layout, show stars, show status icon, include wishlist titles).

### Watched tab

- Bookmory's Achievement tab. A grid of finished titles sorted by finish date, filterable by movies and series.

## Out of scope for now

- Streaks.
- Statistics (daily, annual and tag statistics).
- Rewind.
- Premium features and home screen editing.
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
