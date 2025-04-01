// MainActivity.kt
package com.example.btl1mobile

import android.view.MotionEvent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.btl1mobile.data.models
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.collision.HitResult
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
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberScene
import io.github.sceneview.rememberView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ModelViewer() {
    var isControl by remember { mutableStateOf(false) }
    // An Engine instance main function is to keep track of all resources created by the user and manage
// the rendering thread as well as the hardware renderer.
// To use filament, an Engine instance must be created first.
    val engine = rememberEngine()
// Encompasses all the state needed for rendering a [Scene].
// [View] instances are heavy objects that internally cache a lot of data needed for
// rendering. It is not advised for an application to use many View objects.
// For example, in a game, a [View] could be used for the main scene and another one for the
// game's user interface. More [View] instances could be used for creating special
// effects (e.g. a [View] is akin to a rendering pass).
    val view = rememberView(engine)
// A [Renderer] instance represents an operating system's window.
// Typically, applications create a [Renderer] per window. The [Renderer] generates drawing
// commands for the render thread and manages frame latency.
    val renderer = rememberRenderer(engine)
// Provide your own instance if you want to share [Node]s' scene between multiple views.
    val scene = rememberScene(engine)
// Consumes a blob of glTF 2.0 content (either JSON or GLB) and produces a [Model] object, which is
// a bundle of Filament textures, vertex buffers, index buffers, etc.
// A [Model] is composed of 1 or more [ModelInstance] objects which contain entities and components.
    val modelLoader = rememberModelLoader(engine)
// A Filament Material defines the visual appearance of an object.
// Materials function as a templates from which [MaterialInstance]s can be spawned.
    val materialLoader = rememberMaterialLoader(engine)
// Utility for decoding an HDR file or consuming KTX1 files and producing Filament textures,
// IBLs, and sky boxes.
// KTX is a simple container format that makes it easy to bundle miplevels and cubemap faces
// into a single file.
    val environmentLoader = rememberEnvironmentLoader(engine)
// Physics system to handle collision between nodes, hit testing on a nodes,...
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

    val displayModel = rememberNodes {
        // Add a glTF model
        add(modelNode)
    }

    var totalRotation by remember { mutableFloatStateOf(0f) }

    Column {
        Scene(
            // The modifier to be applied to the layout.
            modifier = Modifier.weight(1f).background(Color.LightGray),
            engine = engine,
            view = view,
            renderer = renderer,
            scene = scene,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environmentLoader = environmentLoader,
            collisionSystem = collisionSystem,
            // Controls whether the render target (SurfaceView) is opaque or not.
            isOpaque = true,
            // Always add a direct light source since it is required for shadowing.
            // We highly recommend adding an [IndirectLight] as well.
            mainLightNode = rememberMainLightNode(engine) {
                intensity = 100_000.0f
            },
            // Load the environement lighting and skybox from an .hdr asset file
//        environment = rememberEnvironment(environmentLoader) {
//            environmentLoader.createHDREnvironment(
//                assetFileLocation = "environments/sky_2k.hdr"
//            )!!
//        },
            // Represents a virtual camera, which determines the perspective through which the scene is
            // viewed.
            // All other functionality in Node is supported. You can access the position and rotation of the
            // camera, assign a collision shape to it, or add children to it.
            cameraNode = rememberCameraNode(engine) {
                // Position the camera 4 units away from the object
                position = Position(z = 1f)
            },
            // Helper that enables camera interaction similar to sketchfab or Google Maps.
            // Needs to be a callable function because it can be reinitialized in case of viewport change
            // or camera node manual position changed.
            // The first onTouch event will make the first manipulator build. So you can change the camera
            // position before any user gesture.
            // Clients notify the camera manipulator of various mouse or touch events, then periodically
            // call its getLookAt() method so that they can adjust their camera(s). Three modes are
            // supported: ORBIT, MAP, and FREE_FLIGHT. To construct a manipulator instance, the desired mode
            // is passed into the create method.
            cameraManipulator = rememberCameraManipulator(),
            // Scene nodes
            childNodes = displayModel,
            // The listener invoked for all the gesture detector callbacks.
            // Detects various gestures and events.
            // The gesture listener callback will notify users when a particular motion event has occurred.
            // Responds to Android touch events with listeners.
//            onGestureListener = rememberOnGestureListener(
//                onDoubleTapEvent = { event, tapedNode ->
//                    // Scale up the tap node (if any) on double tap
//                    tapedNode?.let { it.scale *= 2.0f }
//                }
//            ),
//            // Receive basics on touch event on the view
//            onTouchEvent = { event: MotionEvent, hitResult: HitResult? ->
//                hitResult?.let { println("World tapped : ${it.worldPosition}") }
//                // The touch event is not consumed
//                false
//            },
            // Invoked when an frame is processed.
            // Registers a callback to be invoked when a valid Frame is processing.
            // The callback to be invoked once per frame **immediately before the scene is updated.
            // The callback will only be invoked if the Frame is considered as valid.
            onFrame = { frameTimeNanos ->
                // Increment rotation by a small amount each frame
                totalRotation = (totalRotation + 1f) % 360f

                // Create a fresh rotation object each time instead of modifying the existing one
                modelNode.rotation = Rotation(0f, totalRotation, 0f)}
        )

        Column {
            if (isControl) {
                ModelControlPanel(
                    onMove = { directionX, directionY ->
                        // Apply constant movement speed regardless of thumbstick distance from center
                        // Only the direction matters, not the magnitude
                        if (directionX != 0f || directionY != 0f) {
                            // Normalize the direction vector if it's not zero
                            val length = sqrt(directionX * directionX + directionY * directionY)

                            // Only normalize if length is not zero to avoid division by zero
                            val normalizedX = if (length > 0) directionX / length else 0f
                            val normalizedY = if (length > 0) directionY / length else 0f

                            // Apply constant movement speed
                            val movementSpeed = 0.01f
                            modelNode.position = Position(
                                modelNode.position.x + normalizedX * movementSpeed,
                                modelNode.position.y + normalizedY * movementSpeed,
                                modelNode.position.z
                            )
                        }
                    },
                    onMoveForward = { modelNode.position = Position(modelNode.position.x, modelNode.position.y, modelNode.position.z - 0.1f) },
                    onMoveBackward = { modelNode.position = Position(modelNode.position.x, modelNode.position.y, modelNode.position.z + 0.1f) },
                    onRotateX = { modelNode.rotation = Rotation(modelNode.rotation.x + 10f, modelNode.rotation.y, modelNode.rotation.z) },
                    onRotateY = { modelNode.rotation = Rotation(modelNode.rotation.x, modelNode.rotation.y + 10f, modelNode.rotation.z) },
                    onRotateZ = { modelNode.rotation = Rotation(modelNode.rotation.x, modelNode.rotation.y, modelNode.rotation.z + 10f) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = { isControl = true },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Di Chuyển")
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color.hsl(214f, 0.85f, 0.55f))
                ){
                    Text(stringResource(R.string.main_menu),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
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
    onRotateX: () -> Unit,
    onRotateY: () -> Unit,
    onRotateZ: () -> Unit,
    onReset: () -> Unit,
    onTurnBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Model Controls",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(onClick = onTurnBack) {
                Text("Go Back")
            }
        }

        // Position Controls with Thumbstick
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Movement", fontWeight = FontWeight.Medium)
                Thumbstick(onMove = onMove)
            }

            Column {
                Text("Z-Axis", fontWeight = FontWeight.Medium)
                Row {
                    Button(onClick = onMoveForward) {
                        Text("Zoom Out")
                    }
                    Button(onClick = onMoveBackward) {
                        Text("Zoom In")
                    }
                }
            }
        }

        // Rotation Controls
//        Text("Rotation", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(onClick = onRotateX) {
//                Text("Rotate X")
//            }
//            Button(onClick = onRotateY) {
//                Text("Rotate Y")
//            }
//            Button(onClick = onRotateZ) {
//                Text("Rotate Z")
//            }
//        }

        // Reset Button
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Reset Position")
        }
    }
}

@Composable
fun Thumbstick(onMove: (Float, Float) -> Unit) {
    val thumbstickSize = 150.dp
    val thumbSize = 40.dp

    // State for current thumb position
    var thumbPosition by remember { mutableStateOf(Offset.Zero) }
    // State to track if thumbstick is being interacted with
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(thumbstickSize)
            .background(Color.DarkGray, shape = CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray, shape = CircleShape)
        )

        // Thumb/joystick
        Box(
            modifier = Modifier
                .offset {
                    // Constrain thumb position to the bounds of the thumbstick
                    val maxRadius = (thumbstickSize.toPx() - thumbSize.toPx()) / 2
                    val distance = thumbPosition.getDistance()
                    val cappedDistance = minOf(distance, maxRadius)
                    val angle = thumbPosition.getAngle()
                    val x = cappedDistance * cos(angle)
                    val y = cappedDistance * sin(angle)

                    IntOffset(x.toInt(), y.toInt())
                }
                .size(thumbSize)
                .background(Color.Blue, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isPressed = true },
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
                            val maxRadius = (thumbstickSize.toPx() - thumbSize.toPx()) / 2

                            // If distance exceeds maxRadius, normalize the vector
                            val distance = thumbPosition.getDistance()
                            if (distance > maxRadius) {
                                val angle = thumbPosition.getAngle()
                                thumbPosition = Offset(
                                    maxRadius * cos(angle),
                                    maxRadius * sin(angle)
                                )
                            }

                            // Calculate normalized x and y values (-1 to 1)
                            val normalizedX = thumbPosition.x / maxRadius
                            val normalizedY = -thumbPosition.y / maxRadius // Negate Y for expected direction

                            // Call the provided callback with normalized values
                            onMove(normalizedX, normalizedY)
                        }
                    )
                }
        )
    }
}

// Extension functions for Offset to calculate distance and angle
fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
}

fun Offset.getAngle(): Float {
    return atan2(y, x)
}

@Composable
fun ProductButton(productName: String, onClickProduct: () -> Unit) {
    Button(onClick = { onClickProduct() }, modifier = Modifier.padding(4.dp)) {
        Text(productName)
    }
}

@Preview
@Composable
fun ModelViewerPreview() {
    ModelViewer()
}
