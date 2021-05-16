package io.github.takusan23.miniplayercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takusan23.miniplayercompose.ui.theme.MiniPlayerComposeTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniPlayerComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface {
                    MiniPlayer()
                }
            }
        }
    }
}

@Preview
@Composable
fun MiniPlayerPrev() {
    MiniPlayerComposeTheme {
        Surface {
            MiniPlayer()
        }
    }
}

@Composable
fun MiniPlayer() {
    // 親Viewの大きさを取るのに使った。
    BoxWithConstraints {
        val boxWidth = constraints.maxWidth
        val boxHeight = constraints.maxHeight

        val miniPlayerHeight = ((boxWidth / 2) / 16) * 9

        Box(modifier = Modifier.fillMaxSize()) {

            // ミニプレイヤーの位置
            val offsetX = remember { mutableStateOf(0f) }
            val offsetY = remember { mutableStateOf(0f) }

            // パーセンテージ。offsetYの値が変わると自動で変わる
            val progress = offsetY.value / (boxHeight - miniPlayerHeight)
            // ミニプレイヤーの大きさをFloatで。1fから0.5fまで
            val playerWidthProgress = remember { mutableStateOf(1f) }
            // ミニプレイヤーにする場合はtrueに
            val isMiniPlayer = remember { mutableStateOf(false) }
            // 終了ならtrue
            val isEnd = remember { mutableStateOf(false) }
            // 操作中はtrue
            val isDragging = remember { mutableStateOf(false) }
            // アニメーションしながら戻る。isMiniPlayerの値が変わると動く
            // ちなみにJetpack Composeのアニメーションはスタートの値の指定がない。スタートの値はアニメーション開始前の値になる。
            val playerWidthEx = animateFloatAsState(targetValue = when {
                isDragging.value -> playerWidthProgress.value // 操作中なら操作中の場所へ
                isMiniPlayer.value && !isEnd.value -> 0.5f // ミニプレイヤー遷移命令ならミニプレイヤーのサイズへ
                isEnd.value -> 0.5f // 終了時はミニプレイヤーのまま
                else -> 1f // それ以外
            }, finishedListener = { playerWidthProgress.value = it })
            val offSetYEx = animateFloatAsState(targetValue = when {
                isDragging.value -> offsetY.value // 操作中なら操作中の値
                isMiniPlayer.value && !isEnd.value -> (boxHeight - miniPlayerHeight).toFloat() // ミニプレイヤー遷移命令ならミニプレイヤーのサイズへ
                isEnd.value -> boxHeight.toFloat()
                else -> 1f // それ以外
            }, finishedListener = { offsetY.value = it })

            Column(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), offSetYEx.value.roundToInt()) } // ずらす位置
                    .align(alignment = Alignment.TopEnd) // 右下に行くように
                    .draggable(
                        startDragImmediately = true,
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            // どれだけ移動したかが渡される
                            val currentOffsetY = offsetY.value.toInt()
                            when {
                                currentOffsetY in 0..(boxHeight - miniPlayerHeight) -> {
                                    // 通常
                                    offsetY.value += delta.toInt()
                                    playerWidthProgress.value = 1f - (progress / 2)
                                }
                                currentOffsetY > (boxHeight - miniPlayerHeight) -> {
                                    // 終了させる
                                    offsetY.value += delta.toInt()
                                }
                                else -> {
                                    // 画面外突入
                                    offsetY.value = when {
                                        currentOffsetY <= 0 -> 0f
                                        currentOffsetY > (boxHeight - miniPlayerHeight) -> boxHeight.toFloat()
                                        else -> (boxHeight - miniPlayerHeight).toFloat()
                                    }
                                }
                            }
                        },
                        onDragStopped = { velocity ->
                            // スワイプ速度が渡される
                            when {
                                progress < 0.5f -> {
                                    isMiniPlayer.value = false
                                }
                                progress in 0.5f..1f -> {
                                    isMiniPlayer.value = true
                                }
                                else -> {
                                    isMiniPlayer.value = true
                                    isEnd.value = true
                                }
                            }
                            isDragging.value = false
                        },
                        onDragStarted = {
                            isDragging.value = true
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(playerWidthEx.value) // 引数で大きさを決められる
                        .aspectRatio(1.7f) // 16:9を維持
                        .background(Color(0xFF252525)) // 背景色
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_outline_play_arrow_24),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(100.dp),
                    )
                }
                Column(modifier = Modifier) {
                    Text(
                        text = "動画タイトル",
                        fontSize = 20.sp
                    )
                }
            }

            Button(
                onClick = {
                    isMiniPlayer.value = !isMiniPlayer.value
                    isEnd.value = false
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(text = "MiniPlayer Enable/Disable")
            }

        }

    }
}
