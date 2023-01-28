package com.example.toygalaxywatch

import android.os.Bundle
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.toygalaxywatch.theme.ToyGalaxyWatchTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppSample()
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    ToyGalaxyWatchTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}

@Composable
fun WearAppSample() {
    ToyGalaxyWatchTheme {
        val listState = rememberScalingLazyListState()

        val list = mutableListOf<String>()

        IntRange(1, 100).forEach {
            list.add("$it")
        }

        Scaffold(
            timeText = {
                TimeText()
            },
            vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {

            // Modifiers used by our Wear composables.
            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            val iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 32.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                autoCentering = AutoCenteringParams(itemIndex = 0),
                verticalArrangement = Arrangement.Center,
                state = listState
            ) {
                items(list) {
                    ChipExample2(it,contentModifier,iconModifier)
                }
            }
        }
    }
}

@Composable
fun ButtonExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            onClick = { }) {

            Icon(
                imageVector = Icons.Rounded.Phone,
                contentDescription = "triggers phone action",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun TextExample(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.device_shape)
    )
}

@Composable
fun CardExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        appImage = {
            Icon(
                imageVector = Icons.Rounded.Message,
                contentDescription = "triggers open message action",
                modifier = iconModifier
            )
        },
        onClick = { },
        appName = { Text("Message") },
        time = { Text(text = "12m") },
        title = {
            Text(
                text = "Kim Green"
            )
        }) {
        Text(text = "On my way!")
    }
}

@Composable
fun ChipExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    //overFlow 확인해보기.
    Chip(
        modifier = modifier,
        onClick = { },
        label = {
            Text(
                text = "5 minute Meditation",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }, icon = {
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = "triggers meditation action",
                modifier = iconModifier
            )
        })
}

@Composable
fun ChipExample2(
    string: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    //overFlow 확인해보기.
    Chip(
        modifier = modifier,
        onClick = { },
        label = {
            Text(
                text = string,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }, icon = {
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = "triggers meditation action",
                modifier = iconModifier
            )
        })
}

@Composable
fun ToggleChipExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {

    var checked by remember { mutableStateOf(true) }

    ToggleChip(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { checked = it },
        label = {
            Text(text = "Sound", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        toggleControl = {

        }
    )

}