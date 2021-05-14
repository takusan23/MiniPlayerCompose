package io.github.takusan23.miniplayercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.takusan23.miniplayercompose.ui.theme.MiniPlayerComposeTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniPlayerComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface {


                    BoxWithConstraints {
                        val boxWidth = constraints.maxWidth
                        val boxHeight = constraints.maxHeight

                        val defaultPlayerWidth = boxWidth
                        val defaultPlayerHeight = (boxWidth / 16) * 9
                        val miniPlayerHeight = ((boxWidth / 2) / 16) * 9

                        Box(modifier = Modifier.fillMaxSize()) {

                            // ミニプレイヤーの位置
                            val offsetX = remember { mutableStateOf(0f) }
                            val offsetY = remember { mutableStateOf(0f) }

                            // ミニプレイヤーの大きさ
                            val playerWidth = remember { mutableStateOf(boxWidth) }
                            val playerHeight = remember { mutableStateOf(defaultPlayerHeight) }

                            // パーセンテージ。offsetYの値が変わると自動で変わる
                            val progress = offsetY.value / (boxHeight - miniPlayerHeight)

                            val playerWidthProgress = remember { mutableStateOf(1f) }

                            // ミニプレイヤーにする場合はtrueに
                            val isMiniPlayer = remember { mutableStateOf(false) }
                            // 操作中はtrue
                            val isDraggable = remember { mutableStateOf(false) }
                            // アニメーションしながら戻る。isMiniPlayerの値が変わると動く
                            val playerWidthEx = animateFloatAsState(targetValue = if (isMiniPlayer.value) 0.5f else 1f, finishedListener = { playerWidthProgress.value = it })
                            val offSetYEx = animateFloatAsState(targetValue = if (isMiniPlayer.value) (boxHeight - miniPlayerHeight).toFloat() else 1f, finishedListener = { offsetY.value = it })

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetX.value.roundToInt(), if (isDraggable.value) offsetY.value.roundToInt() else offSetYEx.value.roundToInt()) }
                                    .fillMaxWidth(if (isDraggable.value) playerWidthProgress.value else playerWidthEx.value) // 引数で大きさを決められる
                                    .align(alignment = Alignment.TopEnd) // 右下に行くように
                                    .aspectRatio(1.7f) // 16:9を維持
                                    .background(Color(0xFF252525))
                                    .draggable(
                                        startDragImmediately = true,
                                        orientation = Orientation.Vertical,
                                        state = rememberDraggableState { delta ->
                                            // どれだけ移動したかが渡される
                                            if (offsetY.value.toInt() in 0..(boxHeight - miniPlayerHeight)) {
                                                offsetY.value += delta.toInt()
                                                playerWidthProgress.value = 1f - (progress / 2)
                                                println(progress)
                                            } else {
                                                // 画面外突入
                                                offsetY.value = when {
                                                    offsetY.value.toInt() < 0 -> 0f
                                                    offsetY.value.toInt() > (boxHeight - miniPlayerHeight) -> (boxHeight - miniPlayerHeight).toFloat()
                                                    else -> (boxHeight - miniPlayerHeight).toFloat()
                                                }
                                            }
                                        },
                                        onDragStopped = { velocity ->
                                            // スワイプ速度が渡される
                                            isMiniPlayer.value = progress > 0.5f
                                            isDraggable.value = false
                                        },
                                        onDragStarted = {
                                            isDraggable.value = true
                                        }
                                    )
                            )


                            Button(
                                onClick = { isMiniPlayer.value = !isMiniPlayer.value },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                Text(text = "MiniPlayer Enable/Disable")
                            }

                        }

                    }
                }
            }
        }
    }
}
