import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seatsight.data.repository.BookingRepository
import com.example.seatsight.ui.viewmodel.BookingViewModel

class ViewModelFactory(private val repository: BookingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}