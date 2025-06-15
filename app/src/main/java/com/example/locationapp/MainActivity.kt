package com.example.locationapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyApp(
                        modifier = Modifier,
                        paddingValues = innerPadding,
                        viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier, paddingValues: PaddingValues, viewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    DisplayLocation(paddingValues, context, locationUtils, viewModel)
}

@Composable
fun DisplayLocation(
    paddingValues: PaddingValues,
    context: Context,
    locationUtils: LocationUtils,
    viewModel: LocationViewModel
) {

    val location = viewModel.location.value

    val address = location?.let {
        locationUtils.reverseGeocodeLocation(location)
    }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // we have permission
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            } else {
                // we do not have permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (rationaleRequired) {
                    Toast.makeText(
                        context,
                        "Location Permission Required for this feature",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        "Location Permission Required. Enable in Settings",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

        }
    )

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (location != null) {
            Text("Location: ${location.latitude} ${location.longitude}\nAddress: $address")
        } else {
            Text("Location not found")
        }

        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                // fetch location
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                // request permission
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }) {
            Text("Get Location")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = {
            if (address != null) {
                clipboardManager.setText(AnnotatedString(address.toString()))
                Toast.makeText(context, "Address Copied", Toast.LENGTH_LONG)
                    .show()
            } else{
                Toast.makeText(context, "No Address Found", Toast.LENGTH_LONG)
                    .show()
            }
        }) {
            Text("Copy Address")
        }
    }
}