import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.model.HotelResponse
import com.example.seatsight.data.model.MenuItem
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HotelViewModel(private val repository: HotelRepository) : ViewModel() {
    private val _hotelList = MutableStateFlow<List<HotelResponse>>(emptyList())
    val hotelList: StateFlow<List<HotelResponse>> = _hotelList

    private val _seatList = MutableStateFlow<List<Seat>>(emptyList()) // ✅ Holds seat data
    val seatList: StateFlow<List<Seat>> = _seatList

    private val _menuList = MutableStateFlow<List<MenuItem>>(emptyList()) // ✅ Holds menu data
    val menuList: StateFlow<List<MenuItem>> = _menuList

    fun fetchHotels() {
        viewModelScope.launch {
            try {
                val response = repository.fetchHotels()
                if (response.isSuccessful) {
                    _hotelList.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle Error
            }
        }
    }


    fun fetchSeats(restaurantId: Int) {
        Log.d("FetchSeats", "Fetching seats for restaurantId: $restaurantId") // ✅ Log before API call
        viewModelScope.launch {
            try {
                val response = repository.fetchSeats(restaurantId)
                if (response.isSuccessful) {
                    val rawJson = response.body()?.toString() ?: "Empty Response"
                    Log.d("FetchSeats", "Raw API Response: $rawJson") // ✅ Log raw JSON response

                    val seats = response.body() ?: emptyList()
                    _seatList.value = seats
                    Log.d("FetchSeats", "Parsed Seats: $seats") // ✅ Log parsed seat list
                } else {
                    Log.e("FetchSeats", "API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FetchSeats", "Exception: ${e.message}")
            }
        }
    }

    fun fetchMenu(restaurantId: Int) { // ✅ New method for fetching menu
        viewModelScope.launch {
            try {
                val response = repository.fetchMenu(restaurantId)
                if (response.isSuccessful) {
                    _menuList.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle Error
            }
        }
    }

}



