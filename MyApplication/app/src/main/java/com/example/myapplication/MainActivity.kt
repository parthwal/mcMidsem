package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.assignmentonemc.model.LocationTiles
import com.example.assignmentonemc.model.tileList
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "station_table")
data class Station(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val distance: Float,
    val type: String
)

@Dao
interface StationDao {
    @Insert
    suspend fun insert(station: Station)

    @Update
    suspend fun update(station: Station)

    @Delete
    suspend fun delete(station: Station)

    @Query("SELECT * FROM station_table")
    suspend fun getAllStations(): List<Station>
}
@Database(entities = [Station::class], version = 1, exportSchema = false)
abstract class StationDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

    // Singleton instance
    companion object {
        // Volatile means the reference is always up-to-date and the same to all execution threads.
        @Volatile
        private var INSTANCE: StationDatabase? = null

        fun getDatabase(context: Context): StationDatabase {
            // If instance is null make a new database instance
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StationDatabase::class.java,
                    "station_database"
                ).build()
                INSTANCE = instance
                // Return instance
                instance
            }
        }
    }
}

const val CHANNEL_ID = "channelID"

class MainActivity : ComponentActivity() {
    // Register the permission callback using the new API
    private lateinit var database: StationDatabase
    private lateinit var stationDao: StationDao
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue with the action that requires this permission.
                showNotification()
            } else {
                // Explain to the user that the feature is unavailable without the required permission.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = StationDatabase.getDatabase(this)
        stationDao = database.stationDao()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    createNotificationChannel()
                    Column {
                        Spacer(modifier = Modifier.height(60.dp)) // Adjust the height as needed
                        Greeting("Android"){
                            handleTripEnd()
                        }
                        AddStationUI()
                    }
                }
            }
        }
    }
    private fun insertStation(station: Station) {
        CoroutineScope(Dispatchers.IO).launch {
            stationDao.insert(station)
        }
    }
    private fun updateStation(station: Station) {
        CoroutineScope(Dispatchers.IO).launch {
            stationDao.update(station)
        }
    }
    private fun deleteStation(station: Station) {
        CoroutineScope(Dispatchers.IO).launch {
            stationDao.delete(station)
        }
    }
    private fun getAllStations() {
        CoroutineScope(Dispatchers.IO).launch {
            val stations = stationDao.getAllStations()
            // Do something with the stations, like updating UI
            // Remember to switch to the Main thread if updating UI
        }
    }
    private fun handleTripEnd(){
                    // Check for permission before showing the notification
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is already granted, show the notification
                        showNotification()
                    } else {
                        // Permission not granted, request it
                        requestPermission()
                    }
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_icon_background)
            .setContentTitle("test")
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }

    private fun requestPermission() {
        // Use the new permission API to request the required permission
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "First Channel", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Test Channel Description"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onTripEnd:()->Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isMetric by remember { mutableStateOf(true) }
//    if (currentIndex == tileList.size - 1) {
//             createNotification(LocalContext.current)
//    }
    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hello, User!",
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF111111),
                letterSpacing = 0.1.sp // Adjust the letter spacing as needed
            )

            Text(
                text = "Here are your trip details",
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF111111),
                letterSpacing = 0.05.sp // Adjust the letter spacing as needed
            )
            LazyRow(
                modifier = Modifier
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
            ) {
                item(4) {
                    if (currentIndex == tileList.size - 1) {
                        Button(
                            onClick = {
                                // Handle action for Trip Ended
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Gray
                            ),
                            modifier = Modifier.padding(10.dp),
                            enabled = false
                        ) {
                            Text(
                                text = "Trip Ended",
                                color = Color.Gray
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentIndex < tileList.size - 1) {
                                    currentIndex++
                                    if(currentIndex==tileList.size-1){
                                        onTripEnd()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111)),
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(text = if (currentIndex == 0) "Start Trip" else "Next Stop")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentIndex > 0) currentIndex--
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111)),
                        modifier = Modifier.padding(10.dp),
                        enabled = currentIndex > 0 // Disable the button when at index 0
                    ) {
                        Text(text = "Previous Stop")
                    }

                    Button(
                        onClick = {
                            isMetric = !isMetric
                        },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color(0xFF111111)),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = if (isMetric) "Switch to Miles" else "Switch to Kilometers",
                            color = Color(0xFF111111)
                        )
                    }

                    Button(
                        onClick = {
                            currentIndex = 0
                            onTripEnd()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Red
                        ),
                        border = BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.padding(10.dp),
                        enabled = true
                    ) {
                        Text(
                            text = "Reset Trip",
                            color = Color.Red
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Text(
                    text = "Trip Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                LinearProgressIndicator(
                    progress = currentIndex.toFloat() / (tileList.size - 1), // Adjusted to start from 0
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF111111)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.Start) {
                    Text(text = "Total Distance Travelled: ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    val totalDistance = calculateTotalDistance(currentIndex, isMetric)
                    Text(text = totalDistance, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.End) {
                    Text(text = "Stations Left: ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    val progress=tileList.size- (currentIndex +1)
                    Text(text = progress.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        LazyColumn {
            itemsIndexed(tileList) { index, tile ->
                TileCard(tile = tile, isFirstItem = index == currentIndex, isMetric)
            }
        }
    }
}

@Composable
fun TileCard(tile: LocationTiles, isFirstItem: Boolean, isMetric: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isFirstItem) Color(0xFF111111) else Color.Transparent),
        border = BorderStroke(1.dp, color = Color(0xFF111111)),
    )
    {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = tile.stationName),
                    color = if (isFirstItem) Color.White else Color(0xFF111111),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp // Adjust the letter spacing as needed
                )

                Text(
                    text = stringResource(id = tile.stopDistance),
                    color = if (isFirstItem) Color.White else Color(0xFF111111),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.05.sp // Adjust the letter spacing as needed
                )

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = tile.stationType),
                    color = if (isFirstItem) Color.White else Color(0xFF111111),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.05.sp // Adjust the letter spacing as needed
                )

                Text(
                    text = if (isMetric) (stringResource(id = tile.distance) + " km") else convertKmToMiles(
                        stringResource(id = tile.distance)
                    ),
                    color = if (isFirstItem) Color.White else Color(0xFF111111),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp // Adjust the letter spacing as needed
                )
            }
        }
    }
}

fun convertKmToMiles(km: String): String {
    val miles = km.toFloat() * 0.621371
    return String.format("%.2f", miles) + " mi"
}
@Composable
fun calculateTotalDistance(currentIndex: Int, isMetric: Boolean): String {
    var totalDistance = 0f

    for (i in 0..currentIndex) {
        val distance = stringResource(id = tileList[i].distance).toFloat()
        totalDistance += distance
    }

    return if (isMetric) {
        String.format("%.2f km", totalDistance)
    } else {
        val miles = totalDistance * 0.621371
        String.format("%.2f mi", miles)
    }
}

@Composable
fun AddStationUI() {
    var stationName by remember { mutableStateOf("") }
    var stationDistance by remember { mutableStateOf("") }
    var stationType by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Station Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = stationDistance,
            onValueChange = { stationDistance = it },
            label = { Text(text = "Distance")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = {
                addStationToDatabase(context, stationName, stationDistance, stationType)
                stationName = ""
                stationDistance = ""
                stationType = ""
            })
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = stationType,
            onValueChange = { stationType = it },
            label = { Text("Station Type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                addStationToDatabase(context, stationName, stationDistance, stationType)
                stationName = ""
                stationDistance = ""
                stationType = ""
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Station")
        }
    }
}

private fun addStationToDatabase(context: Context, name: String, distance: String, type: String) {
    val distanceValue = distance.toFloatOrNull() ?: 0f
    val newStation = Station(name = name, distance = distanceValue, type = type)

    CoroutineScope(Dispatchers.IO).launch {
        val stationDao = StationDatabase.getDatabase(context).stationDao()
        stationDao.insert(newStation)
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            Greeting("Android", onTripEnd = {})

        }
    }
}