package dev.gaens.filmory

import android.app.Activity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.getKoin

/** Fills an empty database with sample titles so screens can be checked against the mockup. */
class SeedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Debug tool only: blocking keeps the seeding finished before the activity closes.
        runBlocking { DebugSeeder(getKoin()).seed() }
        Log.i("Filmory", "Seeded sample data")
        finish()
    }
}
