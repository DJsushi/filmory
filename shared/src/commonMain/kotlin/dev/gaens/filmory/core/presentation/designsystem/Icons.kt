package dev.gaens.filmory.core.presentation.designsystem

import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.ic_check
import dev.gaens.filmory.resources.ic_flag
import dev.gaens.filmory.resources.ic_heart
import dev.gaens.filmory.resources.ic_pause
import dev.gaens.filmory.resources.ic_play
import org.jetbrains.compose.resources.DrawableResource

val TitleStatus.icon: DrawableResource
    get() = when (this) {
        TitleStatus.NOT_WATCHED -> Res.drawable.ic_heart
        TitleStatus.WATCHING -> Res.drawable.ic_play
        TitleStatus.WATCHED -> Res.drawable.ic_check
        TitleStatus.ON_HOLD -> Res.drawable.ic_pause
        TitleStatus.DROPPED -> Res.drawable.ic_flag
    }
