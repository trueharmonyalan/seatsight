import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatsight.UI.ViewSeatsWindow
import com.example.seatsight.UI.displayListMenu
import com.example.seatsight.UI.surfaceColor
import com.example.seatsight.data.hotels
import com.example.seatsight.ui.theme.SeatsightTheme

@Composable
 fun BookSeatWindow(modifier: Modifier = Modifier){


    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 1.dp, horizontal = 10.dp)
                .background(surfaceColor)
        ) {




            Surface(
                modifier = Modifier
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(surfaceColor)
                        .fillMaxWidth()
                        .fillMaxHeight()


                ) {

                    Surface {
                        Column(
                            modifier = Modifier
                                .background(surfaceColor)

                        ) {
                            Text(text = "Available Hotels ",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 18.dp)
                                    .padding(start = 10.dp))

                            displayListMenu(hotelDetail = hotels)
                        }
                    }

                }

            }

        }

    }


}

@Preview
@Composable
fun bookSeatWindow(){
    SeatsightTheme {
        BookSeatWindow()
    }
}