package com.example.musicplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {

    lateinit var player: ExoPlayer

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this).build()

        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val window = this.window
                    val insetsController = window.insetsController

                    val colours = listOf(
                        Color(0xFFE9C7C7),
                        Color(0xFFD8C3CC),
                        Color(0xFFC6C2D2),
                        Color(0xFFBFCADA),
                        Color(0xFFBAC6C4),
                        Color(0xFFE69E8F),
                        Color(0xFFEECAE3),
                        Color(0xFFC9BFCE),
                        Color(0xFFF5DDE3),
                    )

                    val darkColours = listOf(
                        Color(0xFF8B6C6C),
                        Color(0xFF73696F),
                        Color(0xFF6B687C),
                        Color(0xFF616F7D),
                        Color(0xFF5E6C6B),
                        Color(0xFF8B5D52),
                        Color(0xFF8E7082),
                        Color(0xFF766D77),
                        Color(0xFF8B7479)
                    )

                    val colourIndex = remember {
                        mutableIntStateOf(0)
                    }

                    LaunchedEffect(Unit) {
                        colourIndex.intValue += 1
                    }

                    LaunchedEffect(colourIndex.intValue) {
                        delay(2100)
                        if (colourIndex.intValue < darkColours.lastIndex) {
                            colourIndex.intValue += 1
                        } else {
                            colourIndex.intValue = 0
                        }
                    }

                    val animatedColour by animateColorAsState(
                        targetValue = colours[colourIndex.intValue],
                        animationSpec = tween(500)
                    )
                    val animatedDarkColour by animateColorAsState(
                        targetValue = darkColours[colourIndex.intValue],
                        animationSpec = tween(500)
                    )

                    LaunchedEffect(animatedColour) {
                        window.statusBarColor = animatedColour.toArgb()
                        window.navigationBarColor = animatedColour.toArgb()

                        if (insetsController != null) {
                            if (animatedColour.luminance() > 0.5) {
                                insetsController.setSystemBarsAppearance(
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                )
                            } else {
                                insetsController.setSystemBarsAppearance(
                                    0,
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                )
                            }
                        }
                    }

                    val musicList = listOf(
                        Music(
                            name = "Maya",
                            cover = R.drawable.maya,
                            music = R.raw.maya
                        ),
                        Music(
                            name = "Me&U",
                            cover = R.drawable.me_u,
                            music = R.raw.me_u
                        ),
                        Music(
                            name = "Won't",
                            cover = R.drawable.wont,
                            music = R.raw.wont
                        )
                    )

                    val pagerState = rememberPagerState(pageCount = {musicList.count()})

                    val playingIndex = remember {
                        mutableIntStateOf(0)
                    }

                    LaunchedEffect(
                        pagerState.currentPage
                    ) {
                        playingIndex.intValue = pagerState.currentPage
                        player.seekTo(pagerState.currentPage, 0)
                    }

                    LaunchedEffect(Unit) {
                        musicList.forEach{
                            val path = "android.resource://" + packageName + "/" + it.music
                            val mediaItem = MediaItem.fromUri(Uri.parse(path))
                            player.addMediaItem(mediaItem)
                        }
                    }

                    player.prepare()

                    val playing = remember {
                        mutableStateOf(false)
                    }

                    val currrentPosition = remember {
                        mutableLongStateOf(0)
                    }

                    val totalDuration = remember {
                        mutableLongStateOf(0)
                    }

                    val progressLen = remember {
                        mutableStateOf(IntSize(0, 0))
                    }
                    
                    LaunchedEffect(player.isPlaying) {
                        playing.value = player.isPlaying
                    }

                    LaunchedEffect(player.currentPosition) {
                        currrentPosition.longValue = player.currentPosition
                    }
                    
                    LaunchedEffect(player.duration) {
                        if (player.duration > 0) {
                            totalDuration.longValue = player.duration
                        }
                    }
                    
                    LaunchedEffect(player.currentMediaItemIndex) {
                        playingIndex.intValue = player.currentMediaItemIndex
                        pagerState.animateScrollToPage(playingIndex.intValue, animationSpec = tween(500))
                    }

                    var percentReached = currrentPosition.longValue.toFloat() / (if (totalDuration.longValue > 0) totalDuration.longValue else 0).toFloat()
                    if (percentReached.isNaN()) {
                        percentReached = 0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        animatedColour,
                                        animatedDarkColour
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val configuration = LocalConfiguration.current

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val textColor by animateColorAsState(targetValue = if (animatedColour.luminance() > .5f) Color(
                                0xff414141
                            ) else Color.White,
                                animationSpec = tween(2000)
                            )
                            /*AnimatedContent(targetState = playingIndex.intValue, transitionSpec = {
                                (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                            }) {
                                Text(text = musicList[it].name, fontSize = 52.sp, color = Color.White)
                            }*/

                            Text(text = musicList[playingIndex.intValue].name, fontSize = 52.sp, color = textColor)
                            Spacer(modifier = Modifier.height(32.dp))

                            HorizontalPager(
                                modifier = Modifier.fillMaxWidth(),
                                state = pagerState,
                                pageSize = PageSize.Fixed((configuration.screenWidthDp / (1.7)).dp),
                                contentPadding = PaddingValues(horizontal = 85.dp)
                            ) {
                                page ->
                                Card(
                                    modifier = Modifier
                                        .size((configuration.screenWidthDp / (1.7)).dp)
                                        .graphicsLayer {
                                            val pageOffset = (
                                                    (pagerState.currentPage - page) + pagerState
                                                        .currentPageOffsetFraction
                                                    ).absoluteValue
                                            val alphaLerp = lerp(
                                                start = 0.4f,
                                                stop = 1f,
                                                amount = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                            val scaleLerp = lerp(
                                                start = 0.5f,
                                                stop = 1f,
                                                amount = 1f - pageOffset.coerceIn(0f, .5f)
                                            )
                                            alpha = alphaLerp
                                            scaleX = scaleLerp
                                            scaleY = scaleLerp
                                        }
                                        .border(2.dp, Color.White, RectangleShape)
                                        .padding(6.dp),
                                    shape = RectangleShape, //CircleShape
                                ) {
                                    Image(
                                        painter = painterResource(id = musicList[page].cover),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(54.dp))
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(text = convertLongToTxt(currrentPosition.longValue), //Time
                                    modifier = Modifier.width(55.dp),
                                    color = textColor,
                                    textAlign = TextAlign.Center)

                                Box(modifier = Modifier
                                    .fillMaxWidth() //Progress
                                    .weight(1f)
                                    .height(8.dp)
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .onGloballyPositioned {progressLen.value = it.size }
                                    .pointerInput(Unit) { detectTapGestures {
                                        val xPos = it.x
                                        val whereClicked = (xPos.toLong() * totalDuration.longValue) / progressLen.value.width.toLong()
                                        player.seekTo(whereClicked)
                                    } },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth(fraction = if(playing.value) percentReached else 0f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xff414141))
                                    )
                                }

                                Text(text = convertLongToTxt(totalDuration.longValue), //Time
                                    modifier = Modifier.width(55.dp),
                                    color = textColor,
                                    textAlign = TextAlign.Center)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Control(icon = R.drawable.ic_fast_rewind, size = 60.dp, onClick =
                                {
                                    player.seekToPreviousMediaItem()
                                })
                                Control(icon = if(playing.value) R.drawable.ic_pause else R.drawable.ic_play, size = 80.dp, onClick =
                                {
                                    if (playing.value) {
                                        player.pause()
                                    } else {
                                        player.play()
                                    }
                                })
                                Control(icon = R.drawable.ic_fast_forward, size = 60.dp, onClick =
                                {
                                    player.seekToNextMediaItem()
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

fun convertLongToTxt(long: Long): String{
    val sec = long/1000
    val mins = sec/60
    val seconds = sec%60

    val minsStr = if (mins < 10){
        "0$mins"
    } else {
        mins.toString()
    }

    val secStr = if (seconds < 10){
        "0$seconds"
    } else {
        seconds.toString()
    }

    return "$minsStr:$secStr"
}

@Composable
fun Control(icon: Int, size: Dp, onClick:() -> Unit) {
    Box(modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(Color.White)
        .clickable {
            onClick()
        }, contentAlignment = Alignment.Center) {
        Icon(modifier = Modifier.size(size / 2),
            painter = painterResource(id = icon),
            tint = Color(0xff414141),
            contentDescription = null
        )
    }

}

fun Color.luminance(): Double {
    return (0.2126 * red + 0.7152 * green + 0.0722 * blue)
}

data class Music(
    val name: String,
    val cover: Int,
    val music: Int
)