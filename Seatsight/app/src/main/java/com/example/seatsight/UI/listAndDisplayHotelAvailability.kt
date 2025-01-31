package com.example.seatsight.UI

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seatsight.data.HotelDetails

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//hotel's name and description is maintained here
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
@Composable
private fun listMenu(
    modifier: Modifier = Modifier,
    hotel: HotelDetails,
) {
//????????????????????????????????????????????
//    Color variables
    val menuButtonColor = Color(android.graphics.Color.parseColor("#BB0000"))
    val menuTextColor = Color(android.graphics.Color.parseColor("#EFEFEF"))
    val menuContainerColor = Color(android.graphics.Color.parseColor("#F0EBEB"))
//    ?????????????????????????????????????????????????/

    var expandMenuButton by remember { mutableStateOf(false) }
    val expandMenuPadding by animateDpAsState(if (expandMenuButton) 50.dp else 0.dp)





    Surface(
        modifier = modifier.padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(menuContainerColor)
                .padding(bottom = expandMenuPadding),

            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(
                        text = hotel.name,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 0.dp)
                            .padding(start = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = hotel.description,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Button(
                    onClick = { expandMenuButton = !expandMenuButton },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(menuButtonColor)
                ) {
                    Text(
                        text = if (expandMenuButton) "Close Menu" else "Menu",
                        color = menuTextColor
                    )
                }
            }

            // Expanded content : Menu updates are displayed here.
            if (expandMenuButton) {
                Text(
                    text = "Additional menu items or details...",
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }
        }
    }
}



//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//this composable is responsible for the display of scrollable hotel details with each hotel details
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
@Composable
fun displayListMenu(
    modifier: Modifier = Modifier,
    hotelDetail: List<HotelDetails>
) {

    LazyColumn(
        modifier = modifier.padding(horizontal = 0.dp)
    ) {
        items(items = hotelDetail) { hotel ->
            listMenu(hotel = hotel) // Display each hotel
        }
    }
}
//
