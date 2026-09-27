package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.*
import com.example.data.models.*
import com.example.data.remote.GeminiApiService
import com.example.data.remote.WeatherApiService
import com.example.service.AuthManager
import com.example.service.ai.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class FarmSathiRepository(
    private val context: Context,
    val aiService: AIService = GeminiAIService(),
    val aiOrchestrator: FarmSathiAiOrchestrator = FarmSathiAiOrchestrator(aiService)
) {

    private val db = FarmSathiDatabase.getInstance(context)
    private val authManager = AuthManager.getInstance(context)

    val currentUser: StateFlow<User?> = authManager.currentUser
    val isAuthenticated: StateFlow<Boolean> = authManager.isAuthenticated

    // Active User ID Flow
    val currentUserId: Flow<String> = currentUser.map { it?.uid ?: "anonymous" }

    // Reactive Data Flows isolated by Current Authenticated User UID
    @OptIn(ExperimentalCoroutinesApi::class)
    val userFarms: Flow<List<Farm>> = currentUserId.flatMapLatest { uid ->
        db.farmDao().getFarmsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userCrops: Flow<List<Crop>> = currentUserId.flatMapLatest { uid ->
        db.cropDao().getCropsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userScans: Flow<List<CropScan>> = currentUserId.flatMapLatest { uid ->
        db.scanDao().getScansForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userChatMessages: Flow<List<ChatMessage>> = currentUserId.flatMapLatest { uid ->
        db.chatMessageDao().getMessagesForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userNotifications: Flow<List<FarmNotification>> = currentUserId.flatMapLatest { uid ->
        db.notificationDao().getNotificationsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSoilReadings: Flow<List<SoilReading>> = currentUserId.flatMapLatest { uid ->
        db.soilDao().getSoilReadingsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTelemetry: Flow<List<TelemetryData>> = currentUserId.flatMapLatest { uid ->
        db.telemetryDao().getTelemetryForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSellingRequests: Flow<List<SellingRequest>> = currentUserId.flatMapLatest { uid ->
        db.sellingRequestDao().getRequestsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userLogisticsRequests: Flow<List<LogisticsRequest>> = currentUserId.flatMapLatest { uid ->
        db.logisticsRequestDao().getLogisticsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userYieldForecasts: Flow<List<YieldForecast>> = currentUserId.flatMapLatest { uid ->
        db.yieldForecastDao().getForecastsForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSyncQueue: Flow<List<SyncQueueItem>> = currentUserId.flatMapLatest { uid ->
        db.syncQueueDao().getSyncQueueForUser(uid).map { list -> list.map { it.toDomain() } }
    }

    // Weather State
    private val _weatherState = MutableStateFlow(WeatherData())
    val weatherState: StateFlow<WeatherData> = _weatherState.asStateFlow()

    private val _weatherLoading = MutableStateFlow(false)
    val weatherLoading: StateFlow<Boolean> = _weatherLoading.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    suspend fun seedInitialDataForUserIfEmpty(user: User) {
        val farms = db.farmDao().getFarmsForUser(user.uid).first()
        if (farms.isEmpty()) {
            val defaultFarm = Farm(
                id = "farm_default_${user.uid}",
                userId = user.uid,
                name = "Kishan Seva Farm",
                location = user.location,
                area = 4.2,
                soilType = "Black Cotton Soil",
                irrigationType = "Drip Irrigation & Borewell"
            )
            db.farmDao().insertFarm(FarmEntity.fromDomain(defaultFarm))

            val defaultCrop1 = Crop(
                id = "crop_1_${user.uid}",
                userId = user.uid,
                farmId = defaultFarm.id,
                farmName = defaultFarm.name,
                cropName = "Soybean",
                variety = "JS-335 High Yield",
                sowingDate = "2026-06-20",
                harvestDate = "2026-10-15",
                area = 2.5,
                soilType = "Black Clay",
                irrigation = "Rainfed & Drip",
                healthStatus = "Healthy"
            )

            val defaultCrop2 = Crop(
                id = "crop_2_${user.uid}",
                userId = user.uid,
                farmId = defaultFarm.id,
                farmName = defaultFarm.name,
                cropName = "Wheat",
                variety = "GW-322 Sharbati",
                sowingDate = "2026-11-10",
                harvestDate = "2027-03-25",
                area = 1.7,
                soilType = "Alluvial Loam",
                irrigation = "Canal & Sprinkler",
                healthStatus = "Needs Attention"
            )

            db.cropDao().insertCrop(CropEntity.fromDomain(defaultCrop1))
            db.cropDao().insertCrop(CropEntity.fromDomain(defaultCrop2))

            // Initial Welcome Notification
            val welcomeNotif = FarmNotification(
                id = "notif_welcome_${System.currentTimeMillis()}",
                userId = user.uid,
                title = "Welcome to FarmSathi! 🌱",
                message = "Your farm ${defaultFarm.name} has been set up. Tap Scan Crop to diagnose leaf diseases anytime.",
                type = "info",
                destination = "home",
                relatedFarmId = defaultFarm.id
            )
            db.notificationDao().insertNotification(NotificationEntity.fromDomain(welcomeNotif))

            // Initial Soil Reading
            val initialSoil = SoilReading(
                id = "soil_${user.uid}_1",
                userId = user.uid,
                farmId = defaultFarm.id,
                farmName = defaultFarm.name,
                ph = 6.8,
                nitrogen = 132.0,
                phosphorus = 24.5,
                potassium = 195.0,
                soilMoisture = 28.5,
                soilTemperature = 25.2,
                electricalConductivity = 0.85,
                organicMatter = 1.3,
                soilType = "Black Cotton Soil",
                dataSource = "IoT Sensor"
            )
            db.soilDao().insertSoilReading(SoilEntity.fromDomain(initialSoil))

            // Initial Telemetry Node
            val initialTelemetry = TelemetryData(
                id = "telemetry_${user.uid}_1",
                userId = user.uid,
                farmId = defaultFarm.id,
                deviceId = "ESP32_AGRI_NODE_01",
                airTemperature = 30.5,
                humidity = 62.0,
                soilMoisture = 26.0,
                soilTemperature = 24.8,
                rainfall = 0.0,
                windSpeed = 11.5,
                lightIntensity = 38000.0,
                leafWetness = 12.0,
                dataSource = "IoT Sensor"
            )
            db.telemetryDao().insertTelemetry(TelemetryEntity.fromDomain(initialTelemetry))

            // Initial Yield Forecast
            val initialForecast = com.example.service.ForecastingAndAdvisoryEngine.generateYieldAndHarvestForecast(
                crop = defaultCrop1,
                soil = initialSoil,
                telemetry = initialTelemetry,
                weather = weatherState.value
            )
            db.yieldForecastDao().insertForecast(YieldForecastEntity.fromDomain(initialForecast))
        }
    }

    suspend fun refreshWeather(lat: Double = 23.2599, lng: Double = 77.4126, locationName: String = "Indore, MP") {
        _weatherLoading.value = true
        _weatherError.value = null

        val result = WeatherApiService.fetchLiveWeather(lat, lng, locationName)
        result.onSuccess { data ->
            _weatherState.value = data
            _weatherLoading.value = false
        }.onFailure { err ->
            _weatherLoading.value = false
            _weatherError.value = "Live weather update unavailable: ${err.message}"
        }
    }

    // Farm CRUD
    suspend fun addFarm(farm: Farm) {
        db.farmDao().insertFarm(FarmEntity.fromDomain(farm))
    }

    suspend fun updateFarm(farm: Farm) {
        db.farmDao().updateFarm(FarmEntity.fromDomain(farm))
    }

    suspend fun deleteFarm(farmId: String, userId: String) {
        db.farmDao().deleteFarm(farmId, userId)
    }

    // Crop CRUD
    suspend fun addCrop(crop: Crop) {
        db.cropDao().insertCrop(CropEntity.fromDomain(crop))
    }

    suspend fun updateCrop(crop: Crop) {
        db.cropDao().updateCrop(CropEntity.fromDomain(crop))
    }

    suspend fun deleteCrop(cropId: String, userId: String) {
        db.cropDao().deleteCrop(cropId, userId)
    }

    // AI Multimodal Scan
    suspend fun analyzeAndSaveCropScan(
        bitmap: Bitmap,
        cropName: String,
        userId: String,
        cropId: String
    ): Result<CropScan> {
        val result = aiService.analyzeCropImage(bitmap, cropName, userId, cropId)
        result.onSuccess { scan ->
            db.scanDao().insertScan(ScanEntity.fromDomain(scan))

            // Add notification if issue detected
            if (scan.severity == "High" || scan.needsExpert) {
                val notif = FarmNotification(
                    id = "notif_scan_${System.currentTimeMillis()}",
                    userId = userId,
                    title = "Attention: ${scan.cropName} Disease Alert",
                    message = "Scan detected ${scan.possibleIssue}. Review recommended treatments.",
                    type = "scan",
                    destination = "cropDoctor",
                    relatedCropId = cropId
                )
                db.notificationDao().insertNotification(NotificationEntity.fromDomain(notif))
            }
        }
        return result
    }

    // AI Chat & Orchestration
    suspend fun sendChatMessage(
        userText: String,
        userId: String,
        farmerContext: FarmAIContext
    ): Result<AIResponse> {
        val userMsg = ChatMessage(
            id = "msg_user_${System.currentTimeMillis()}",
            userId = userId,
            sender = "user",
            text = userText
        )
        db.chatMessageDao().insertMessage(ChatMessageEntity.fromDomain(userMsg))

        val historyList = db.chatMessageDao().getMessagesForUser(userId).first().map {
            Pair(it.sender, it.text)
        }

        val result = aiOrchestrator.processQuery(userText, farmerContext, historyList)
        result.onSuccess { aiResp ->
            val aiMsg = ChatMessage(
                id = "msg_ai_${System.currentTimeMillis()}",
                userId = userId,
                sender = "ai",
                text = aiResp.answer
            )
            db.chatMessageDao().insertMessage(ChatMessageEntity.fromDomain(aiMsg))
        }
        return result
    }

    // Legacy overload for compatibility
    suspend fun sendChatMessage(userText: String, userId: String, farmerContext: String): Result<String> {
        val userMsg = ChatMessage(
            id = "msg_user_${System.currentTimeMillis()}",
            userId = userId,
            sender = "user",
            text = userText
        )
        db.chatMessageDao().insertMessage(ChatMessageEntity.fromDomain(userMsg))

        val historyList = db.chatMessageDao().getMessagesForUser(userId).first().map {
            Pair(it.sender, it.text)
        }

        val dummyContext = FarmAIContext(
            farmerName = "Farmer",
            location = "Farm Location"
        )

        val result = aiOrchestrator.processQuery(userText, dummyContext, historyList)
        return result.map { aiResp ->
            val aiMsg = ChatMessage(
                id = "msg_ai_${System.currentTimeMillis()}",
                userId = userId,
                sender = "ai",
                text = aiResp.answer
            )
            db.chatMessageDao().insertMessage(ChatMessageEntity.fromDomain(aiMsg))
            aiResp.answer
        }
    }

    suspend fun clearChat(userId: String) {
        db.chatMessageDao().clearChatForUser(userId)
    }

    // Notifications
    suspend fun markNotificationsRead(userId: String) {
        db.notificationDao().markAllAsRead(userId)
    }

    suspend fun deleteNotification(id: String, userId: String) {
        db.notificationDao().deleteNotification(id, userId)
    }

    // Soil & Telemetry CRUD
    suspend fun addSoilReading(soil: SoilReading) {
        db.soilDao().insertSoilReading(SoilEntity.fromDomain(soil))
    }

    suspend fun addTelemetry(telemetry: TelemetryData) {
        db.telemetryDao().insertTelemetry(TelemetryEntity.fromDomain(telemetry))
    }

    // Market & Logistics
    suspend fun addSellingRequest(request: SellingRequest) {
        db.sellingRequestDao().insertRequest(SellingRequestEntity.fromDomain(request))
        // Add to offline sync queue if needed
        val syncItem = SyncQueueItem(
            id = "sync_${System.currentTimeMillis()}",
            userId = request.userId,
            actionType = "CREATE_SELLING_REQUEST",
            payloadSummary = "Selling ${request.quantityQuintals} Qtl ${request.cropName} to ${request.buyerName}"
        )
        db.syncQueueDao().insertSyncItem(SyncQueueEntity.fromDomain(syncItem))
    }

    suspend fun addLogisticsRequest(request: LogisticsRequest) {
        db.logisticsRequestDao().insertLogistics(LogisticsRequestEntity.fromDomain(request))
        val syncItem = SyncQueueItem(
            id = "sync_${System.currentTimeMillis()}",
            userId = request.userId,
            actionType = "BOOK_LOGISTICS",
            payloadSummary = "Logistics Booking: ${request.vehicleType} to ${request.destinationMandi}"
        )
        db.syncQueueDao().insertSyncItem(SyncQueueEntity.fromDomain(syncItem))
    }

    suspend fun addYieldForecast(forecast: YieldForecast) {
        db.yieldForecastDao().insertForecast(YieldForecastEntity.fromDomain(forecast))
    }

    // Offline Sync
    suspend fun syncOfflineQueue(userId: String) {
        db.syncQueueDao().markAllSynced(userId)
    }
}
