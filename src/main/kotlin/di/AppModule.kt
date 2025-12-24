package di

import data.repository.AuthRepository
import data.repository.BookingRepository
import data.repository.ClientRepository
import data.repository.TourRepository
import org.koin.dsl.module
import ui.screens.auth.LoginScreenModel
import ui.screens.bookings.BookingDialogModel
import ui.screens.tours.ToursScreenModel
import ui.screens.bookings.BookingsScreenModel
import data.repository.PaymentRepository
import ui.screens.finance.FinanceScreenModel
import ui.screens.clients.ClientsScreenModel
import data.repository.AdminRepository
import ui.screens.admin.AdminScreenModel
import data.repository.ReportRepository
import ui.screens.reports.ReportsScreenModel
import ui.screens.auth.RegisterScreenModel
import data.repository.ReviewRepository
import ui.screens.reports.ReviewsScreenModel // Импорт


// Здесь мы описываем, как создавать наши классы
val appModule = module {

    // single = Синглтон. Объект создается один раз при старте
    // и используется во всем приложении.
    // Это идеально подходит для Репозиториев.

    single { AuthRepository() }
    single { TourRepository() }
    single { ClientRepository() }
    single { BookingRepository() }
    single { PaymentRepository() }
    single { AdminRepository() }
    single { ReportRepository() }
    single { ReviewRepository() }
    single { ReviewRepository() }


    // В будущем здесь мы добавим ViewModels (ScreenModels)
    // Для них мы будем использовать factory { ... }
    factory { LoginScreenModel(get()) }
    factory { ToursScreenModel(get()) }
    factory { BookingDialogModel(get(), get()) }
    factory { FinanceScreenModel(get()) }
    factory { ClientsScreenModel(get()) }
    factory { AdminScreenModel(get()) }
    factory { ReportsScreenModel(get()) }
    factory { LoginScreenModel(get()) }
    factory { RegisterScreenModel(get()) }
    factory { BookingsScreenModel(get(), get(), get()) }
    factory { ReviewsScreenModel(get()) }
}

