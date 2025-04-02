// MainActivity.kt
package com.example.btl1mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.btl1mobile.data.models
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberScene
import io.github.sceneview.rememberView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModelViewer(onReturn: () -> Unit) {
    var isControl by remember { mutableStateOf(false) }
    val engine = rememberEngine()
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine)
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val collisionSystem = rememberCollisionSystem(view)

    var modelNode by remember {
        mutableStateOf(ModelNode(
            // Load it from a binary .glb in the asset files
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/iron.glb"
            ),
            scaleToUnits = 0.3f
        ))
    }

    val displayModel = rememberNodes { add(modelNode) }

    var totalRotation by remember { mutableFloatStateOf(0f) }
    var moveX by remember { mutableFloatStateOf(0f) }
    var moveY by remember { mutableFloatStateOf(0f) }

    Column {
        Scene(
            // The modifier to be applied to the layout.
            modifier = Modifier
                .weight(1f)
                .background(Color.LightGray),
            engine = engine,
            view = view,
            renderer = renderer,
            scene = scene,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environmentLoader = environmentLoader,
            collisionSystem = collisionSystem,
            isOpaque = true,
            mainLightNode = rememberMainLightNode(engine) {
                intensity = 100_000.0f
            },
            cameraNode = rememberCameraNode(engine) {
                // Position the camera 4 units away from the object
                position = Position(z = 1f)
            },
            cameraManipulator = rememberCameraManipulator(),
            // Scene nodes
            childNodes = displayModel,
            onFrame = { frameTimeNanos ->
                val moveSpeed = 0.01f
                // Increment rotation by a small amount each frame
                totalRotation = (totalRotation + 1f) % 360f

                // Create a fresh rotation object each time instead of modifying the existing one
                modelNode.rotation = Rotation(0f, totalRotation, 0f)

                modelNode.position = Position(
                    modelNode.position.x + moveX * moveSpeed,
                    modelNode.position.y + moveY * moveSpeed,
                    modelNode.position.z
                )
            }

        )

        Column {
            if (isControl) {
                ModelControlPanel(
                    onMove = { directionX, directionY ->
                        moveX = directionX
                        moveY = directionY
                    },
                    onMoveForward = { modelNode.position = Position(modelNode.position.x, modelNode.position.y, modelNode.position.z - 0.1f) },
                    onMoveBackward = { modelNode.position = Position(modelNode.position.x, modelNode.position.y, modelNode.position.z + 0.1f) },
                    onReset = {
                        modelNode.position = Position(0f, 0f, 0f)
                        modelNode.rotation = Rotation(0f, 0f, 0f)
                        modelNode.scale = Float3(1f)
                    },
                    onTurnBack = {
                        isControl = false
                    }
                )
            }
            else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = { isControl = true },
                        modifier = Modifier.padding(8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.model_animate))
                    }

                    Button(
                        onClick = onReturn,
                        modifier = Modifier.padding(8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.back))
                    }
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .background(Color.hsl(234f, 0.71f, 0.91f))
                ) {
                    models.forEach { model ->
                        ProductButton(
                            model.key,
                            onClickProduct = {
                                modelNode = ModelNode(
                                    // Load it from a binary .glb in the asset files
                                    modelInstance = modelLoader.createModelInstance(
                                        assetFileLocation = "models/${model.value}"
                                    ),
                                    scaleToUnits = 0.3f
                                )
                                displayModel[0] = modelNode
                            }
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ModelControlPanel(
    onMove: (Float, Float) -> Unit,
    onMoveForward: () -> Unit,
    onMoveBackward: () -> Unit,
    onReset: () -> Unit,
    onTurnBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.controller_heading),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextButton(
                onClick = onTurnBack
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back Icon")
                Text(text = stringResource(R.string.back))
            }
        }

        // Position Controls with joystick
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Thumbstick(onMove = onMove)
            Column {
                Button(onClick = onMoveForward, modifier = Modifier.width(136.dp)) {
                    Icon(painterResource(R.drawable.zoom_out), contentDescription = "zoom out")
                    Text(stringResource(R.string.zoom_out))
                }
                Button(onClick = onMoveBackward, modifier = Modifier.width(136.dp)) {
                    Icon(painterResource(R.drawable.zoom_in), contentDescription = "zoom in")
                    Text(stringResource(R.string.zoom_in))
                }
            }
        }

        // Reset Button
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Default Position")
            Text(stringResource(R.string.default_position))
        }
    }
}

@Composable
fun Thumbstick(onMove: (Float, Float) -> Unit) {
    val jobStickSize = 150.dp
    val thumbSize = 40.dp

    // State for current thumb position
    var thumbPosition by remember { mutableStateOf(Offset.Zero) }
    // State to track if joystick is being interacted with
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(jobStickSize)
//            .background(Color.DarkGray, shape = CircleShape)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isPressed = true
                        // Calculate initial position relative to center
                        val center = Offset(size.width / 2f, size.height / 2f)
                        thumbPosition = Offset(offset.x - center.x, offset.y - center.y)

                        // Constrain initial position
                        val maxRadius = (jobStickSize.toPx() - thumbSize.toPx()) / 2
                        val distance = thumbPosition.getDistance()
                        if (distance > maxRadius) {
                            val angle = thumbPosition.getAngle()
                            thumbPosition = Offset(
                                maxRadius * cos(angle),
                                maxRadius * sin(angle)
                            )
                        }

                        // Calculate normalized direction
                        val maxDistance = (jobStickSize.toPx() - thumbSize.toPx()) / 2
                        val normalizedX = thumbPosition.x / maxDistance
                        val normalizedY =
                            -thumbPosition.y / maxDistance // Negate Y for expected direction

                        // Send movement regardless of whether we're at the edge
                        onMove(normalizedX, normalizedY)
                    },
                    onDragEnd = {
                        isPressed = false
                        thumbPosition = Offset.Zero
                        onMove(0f, 0f) // Reset movement when user releases
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // Update thumb position
                        thumbPosition += dragAmount

                        // Calculate the maximum distance the thumb can travel
                        val maxRadius = (jobStickSize.toPx() - thumbSize.toPx()) / 2

                        // If distance exceeds maxRadius, normalize the vector but keep direction
                        val distance = thumbPosition.getDistance()
                        val angle = thumbPosition.getAngle()

                        if (distance > maxRadius) {
                            // Clamp thumb position to the edge of the allowed circle
                            thumbPosition = Offset(
                                maxRadius * cos(angle),
                                maxRadius * sin(angle)
                            )
                        }

                        // Calculate normalized direction values (-1 to 1)
                        // This will be the same whether we're at the edge or not
                        val normalizedX = thumbPosition.x / maxRadius
                        val normalizedY =
                            -thumbPosition.y / maxRadius // Negate Y for expected direction


                        // Always send movement based on direction
                        onMove(normalizedX, normalizedY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer ring background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(178f, 0.1f, 0.70f), shape = CircleShape)
        )

        // Thumb/joystick
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(thumbPosition.x.toInt(), thumbPosition.y.toInt())
                }
                .size(thumbSize)
                .background(Color.hsl(178f, 0f, 0.60f), shape = CircleShape)
        )
    }
}

fun Offset.getAngle(): Float {
    return atan2(y, x)
}

@Composable
fun ProductButton(productName: String, onClickProduct: () -> Unit) {
    OutlinedButton(onClick = { onClickProduct() }, modifier = Modifier.padding(4.dp)) {
        Text(productName)
    }
}

@Preview
@Composable
fun ModelViewerPreview() {
    ModelControlPanel(
        onTurnBack = {},
        onMoveForward = {},
        onMoveBackward = {},
        onReset = {},
        onMove = {x, y ->}
    )
}
