import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.model.HotelResponse
import com.example.seatsight.data.repository.HotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HotelViewModel(private val repository: HotelRepository) : ViewModel() {
    private val _hotelList = MutableStateFlow<List<HotelResponse>>(emptyList())
    val hotelList: StateFlow<List<HotelResponse>> = _hotelList

    fun fetchHotels() {
        viewModelScope.launch {
            try {
                val response = repository.fetchHotels()
                if (response.isSuccessful) {
                    _hotelList.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

