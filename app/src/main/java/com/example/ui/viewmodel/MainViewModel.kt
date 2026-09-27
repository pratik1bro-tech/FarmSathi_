package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.FarmSathiRepository
import com.example.service.AuthManager
import com.example.service.SpeechManager
import com.example.service.ai.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmSathiRepository(application)
    private val authManager = AuthManager.getInstance(application)
    val speechManager = SpeechManager(application)
    val aiOrchestrator = repository.aiOrchestrator

    // Auth States
    val currentUser: StateFlow<User?> = repository.currentUser
    val isAuthenticated: StateFlow<Boolean> = repository.isAuthenticated

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Domain Data
    val farms: StateFlow<List<Farm>> = repository.userFarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val crops: StateFlow<List<Crop>> = repository.userCrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scans: StateFlow<List<CropScan>> = repository.userScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.userChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<FarmNotification>> = repository.userNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soilReadings: StateFlow<List<SoilReading>> = repository.userSoilReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telemetryReadings: StateFlow<List<TelemetryData>> = repository.userTelemetry
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellingRequests: StateFlow<List<SellingRequest>> = repository.userSellingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logisticsRequests: StateFlow<List<LogisticsRequest>> = repository.userLogisticsRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yieldForecasts: StateFlow<List<YieldForecast>> = repository.userYieldForecasts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncQueue: StateFlow<List<SyncQueueItem>> = repository.userSyncQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weather
    val weatherData: StateFlow<WeatherData> = repository.weatherState
    val weatherLoading: StateFlow<Boolean> = repository.weatherLoading
    val weatherError: StateFlow<String?> = repository.weatherError

    // Mandi Market & Buyer State
    private val _selectedCommodity = MutableStateFlow("Soybean")
    val selectedCommodity: StateFlow<String> = _selectedCommodity.asStateFlow()

    val mandiPrices: StateFlow<List<MandiPrice>> = _selectedCommodity
        .map { commodityName: String -> com.example.service.MandiMarketEngine.getRegionalMandiPrices(commodityName) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyersList: StateFlow<List<BuyerProfile>> = _selectedCommodity
        .map { commodityName: String -> com.example.service.MandiMarketEngine.getVerifiedBuyers(commodityName) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val marketRepository = com.example.data.repository.MarketRepository(application)

    val recommendedMarketResult: StateFlow<com.example.data.repository.MarketRecommendation?> = mandiPrices
        .map { list: List<MandiPrice> -> if (list.isNotEmpty()) marketRepository.calculateBestMarketForLocation(list) else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Advisories
    val npkAdvisory: StateFlow<NpkAdvisory> = combine(crops, soilReadings) { cList, sList ->
        com.example.service.SoilAndTelemetryEngine.generateNpkAdvisory(cList.firstOrNull(), sList.firstOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.service.SoilAndTelemetryEngine.generateNpkAdvisory(null, null))

    val irrigationAdvisory: StateFlow<IrrigationAdvisory> = combine(crops, telemetryReadings, weatherData) { cList, tList, wData ->
        com.example.service.SoilAndTelemetryEngine.generateIrrigationAdvisory(cList.firstOrNull(), tList.firstOrNull(), wData)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.service.SoilAndTelemetryEngine.generateIrrigationAdvisory(null, null, WeatherData()))

    // AI Scanner
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _currentScanResult = MutableStateFlow<CropScan?>(null)
    val currentScanResult: StateFlow<CropScan?> = _currentScanResult.asStateFlow()

    // AI Intelligence Layer States
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _lastAiResponse = MutableStateFlow<AIResponse?>(null)
    val lastAiResponse: StateFlow<AIResponse?> = _lastAiResponse.asStateFlow()

    private val _dailyBriefing = MutableStateFlow<FarmBriefing?>(null)
    val dailyBriefing: StateFlow<FarmBriefing?> = _dailyBriefing.asStateFlow()

    private val _farmHealthScore = MutableStateFlow<FarmHealthScore?>(null)
    val farmHealthScore: StateFlow<FarmHealthScore?> = _farmHealthScore.asStateFlow()

    // Active Selected Farm / Crop
    private val _selectedFarm = MutableStateFlow<Farm?>(null)
    val selectedFarm: StateFlow<Farm?> = _selectedFarm.asStateFlow()

    private val _selectedCrop = MutableStateFlow<Crop?>(null)
    val selectedCrop: StateFlow<Crop?> = _selectedCrop.asStateFlow()

    init {
        viewModelScope.launch {
            currentUser.collectLatest { user ->
                if (user != null) {
                    repository.seedInitialDataForUserIfEmpty(user)
                    repository.refreshWeather(locationName = user.location)
                    refreshDailyBriefing()
                    refreshFarmHealthScore()
                }
            }
        }
    }

    // Auth actions
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val res = authManager.login(email, pass)
            res.onFailure {
                _authError.value = it.message
            }
            _authLoading.value = false
        }
    }

    fun signup(name: String, email: String, pass: String, phone: String, language: String, location: String, experience: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val res = authManager.signup(name, email, pass, phone, language, location, experience)
            res.onFailure {
                _authError.value = it.message
            }
            _authLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val res = authManager.resetPassword(email)
            res.onSuccess { msg ->
                _authError.value = msg
            }.onFailure { err ->
                _authError.value = err.message
            }
            _authLoading.value = false
        }
    }

    fun logout() {
        authManager.logout()
    }

    fun switchUserForTesting(user: User) {
        viewModelScope.launch {
            authManager.switchUserAccount(user)
        }
    }

    fun updateProfile(name: String, phone: String, language: String, location: String, experience: String) {
        val curr = currentUser.value ?: return
        viewModelScope.launch {
            val updated = curr.copy(
                name = name,
                phone = phone,
                preferredLanguage = language,
                location = location,
                farmingExperience = experience
            )
            authManager.updateUserProfile(updated)
        }
    }

    // Farm CRUD
    fun addFarm(name: String, location: String, area: Double, soilType: String, irrigation: String, notes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newFarm = Farm(
                id = "farm_${System.currentTimeMillis()}",
                userId = user.uid,
                name = name,
                location = location,
                area = area,
                soilType = soilType,
                irrigationType = irrigation,
                notes = notes
            )
            repository.addFarm(newFarm)
        }
    }

    fun deleteFarm(farm: Farm) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteFarm(farm.id, user.uid)
        }
    }

    fun selectFarm(farm: Farm?) {
        _selectedFarm.value = farm
    }

    // Crop CRUD
    fun addCrop(farmId: String, farmName: String, cropName: String, variety: String, sowingDate: String, harvestDate: String, area: Double, notes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newCrop = Crop(
                id = "crop_${System.currentTimeMillis()}",
                userId = user.uid,
                farmId = farmId,
                farmName = farmName,
                cropName = cropName,
                variety = variety,
                sowingDate = sowingDate,
                harvestDate = harvestDate,
                area = area,
                notes = notes,
                healthStatus = "Healthy"
            )
            repository.addCrop(newCrop)
        }
    }

    fun deleteCrop(crop: Crop) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteCrop(crop.id, user.uid)
        }
    }

    fun selectCrop(crop: Crop?) {
        _selectedCrop.value = crop
    }

    // Multimodal Crop Doctor Scan
    fun performCropScan(bitmap: Bitmap, cropName: String, cropId: String = "general") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            _currentScanResult.value = null

            val res = repository.analyzeAndSaveCropScan(bitmap, cropName, user.uid, cropId)
            res.onSuccess { scan ->
                _currentScanResult.value = scan
                _isScanning.value = false
            }.onFailure { err ->
                _scanError.value = err.message
                _isScanning.value = false
            }
        }
    }

    fun clearCurrentScanResult() {
        _currentScanResult.value = null
        _scanError.value = null
    }

    fun getCurrentFarmAIContext(): FarmAIContext {
        val user = currentUser.value
        val farmList = farms.value
        val cropList = crops.value
        val pFarm = selectedFarm.value ?: farmList.firstOrNull()
        val pCrop = selectedCrop.value ?: cropList.firstOrNull()
        val wData = weatherData.value
        val soil = soilReadings.value.firstOrNull()
        val telem = telemetryReadings.value.firstOrNull()
        val mPrices = mandiPrices.value
        val latestScan = scans.value.firstOrNull()

        return FarmAIContext(
            farmerName = user?.name ?: "Farmer",
            location = user?.location ?: pFarm?.location ?: "Madhya Pradesh",
            preferredLanguage = user?.preferredLanguage ?: "English",
            farmingExperience = user?.farmingExperience ?: "Medium",
            primaryFarm = pFarm,
            primaryCrop = pCrop,
            allFarms = farmList,
            allCrops = cropList,
            weather = wData,
            isLiveWeather = wData.locationName.isNotBlank(),
            latestSoil = soil,
            latestTelemetry = telem,
            isSensorLive = telem != null,
            relevantMandiPrices = mPrices,
            isLiveMarketData = mPrices.isNotEmpty(),
            latestCropScan = latestScan
        )
    }

    // Central AI Intelligence Ask
    fun askFarmSathi(query: String, onComplete: ((AIResponse) -> Unit)? = null) {
        val user = currentUser.value ?: return
        if (query.isBlank()) return

        viewModelScope.launch {
            _isChatLoading.value = true
            _chatError.value = null

            val context = getCurrentFarmAIContext()
            val result = repository.sendChatMessage(query, user.uid, context)

            result.onSuccess { response ->
                _lastAiResponse.value = response
                onComplete?.invoke(response)
            }.onFailure { err ->
                _chatError.value = err.message ?: "FarmSathi AI is temporarily unavailable."
            }
            _isChatLoading.value = false
        }
    }

    // AI Chat
    fun sendChatMessage(text: String) {
        askFarmSathi(text)
    }

    fun refreshDailyBriefing() {
        viewModelScope.launch {
            val context = getCurrentFarmAIContext()
            val result = aiOrchestrator.getDailyBriefing(context)
            result.onSuccess {
                _dailyBriefing.value = it
            }
        }
    }

    fun refreshFarmHealthScore() {
        viewModelScope.launch {
            val context = getCurrentFarmAIContext()
            val result = aiOrchestrator.getFarmHealthScore(context)
            result.onSuccess {
                _farmHealthScore.value = it
            }
        }
    }

    fun clearLastAiResponse() {
        _lastAiResponse.value = null
    }

    fun clearChat() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.clearChat(user.uid)
        }
    }

    // Weather Refresh
    fun refreshWeather() {
        val user = currentUser.value
        viewModelScope.launch {
            repository.refreshWeather(locationName = user?.location ?: "Indore, MP")
        }
    }

    // Notifications
    fun markNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markNotificationsRead(user.uid)
        }
    }

    fun deleteNotification(id: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteNotification(id, user.uid)
        }
    }

    // Soil & Telemetry Actions
    fun addSoilReading(ph: Double, n: Double, p: Double, k: Double, moisture: Double, temp: Double, source: String = "Manual Entry") {
        val user = currentUser.value ?: return
        val farm = farms.value.firstOrNull() ?: return
        viewModelScope.launch {
            val reading = SoilReading(
                id = "soil_${System.currentTimeMillis()}",
                userId = user.uid,
                farmId = farm.id,
                farmName = farm.name,
                ph = ph,
                nitrogen = n,
                phosphorus = p,
                potassium = k,
                soilMoisture = moisture,
                soilTemperature = temp,
                dataSource = source
            )
            repository.addSoilReading(reading)
        }
    }

    fun addTelemetryReading(temp: Double, humidity: Double, moisture: Double, soilTemp: Double, rain: Double, source: String = "Manual Entry") {
        val user = currentUser.value ?: return
        val farm = farms.value.firstOrNull() ?: return
        viewModelScope.launch {
            val data = TelemetryData(
                id = "telemetry_${System.currentTimeMillis()}",
                userId = user.uid,
                farmId = farm.id,
                airTemperature = temp,
                humidity = humidity,
                soilMoisture = moisture,
                soilTemperature = soilTemp,
                rainfall = rain,
                dataSource = source
            )
            val valRes = com.example.service.SoilAndTelemetryEngine.validateTelemetry(data)
            if (valRes.isSuccess) {
                repository.addTelemetry(data)
            }
        }
    }

    // Market Actions
    fun selectCommodity(commodity: String) {
        _selectedCommodity.value = commodity
    }

    fun createSellingRequest(buyer: BuyerProfile, quantityQtl: Double, expectedPrice: Double) {
        val user = currentUser.value ?: return
        val cropName = selectedCommodity.value
        viewModelScope.launch {
            val req = SellingRequest(
                id = "sell_${System.currentTimeMillis()}",
                userId = user.uid,
                farmerName = user.name,
                cropName = cropName,
                quantityQuintals = quantityQtl,
                expectedPricePerQuintal = expectedPrice,
                buyerId = buyer.id,
                buyerName = buyer.businessName,
                mandiName = buyer.location,
                status = "Submitted"
            )
            repository.addSellingRequest(req)
        }
    }

    fun bookLogistics(destinationMandi: String, cropName: String, quantityQtl: Double, vehicleType: String, pickupDate: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val distanceKm = 35.0
            val cost = com.example.service.MandiMarketEngine.calculateLogisticsCost(vehicleType, distanceKm, quantityQtl)
            val logReq = LogisticsRequest(
                id = "logistics_${System.currentTimeMillis()}",
                userId = user.uid,
                farmerName = user.name,
                pickupLocation = user.location,
                destinationMandi = destinationMandi,
                cropName = cropName,
                quantityQuintals = quantityQtl,
                vehicleType = vehicleType,
                estimatedDistanceKm = distanceKm,
                estimatedCostInr = cost,
                pickupDate = pickupDate,
                status = "Booked"
            )
            repository.addLogisticsRequest(logReq)
        }
    }

    fun syncOfflineQueue() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.syncOfflineQueue(user.uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
