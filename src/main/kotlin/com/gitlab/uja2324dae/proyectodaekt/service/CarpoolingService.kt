package com.gitlab.uja2324dae.proyectodaekt.service

import com.gitlab.uja2324dae.proyectodaekt.model.SharedRide
import com.gitlab.uja2324dae.proyectodaekt.model.User
import com.gitlab.uja2324dae.proyectodaekt.model.UserRating
import com.gitlab.uja2324dae.proyectodaekt.repository.SharedRideRepository
import com.gitlab.uja2324dae.proyectodaekt.repository.UserRatingRepository
import com.gitlab.uja2324dae.proyectodaekt.repository.UserRepository
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.sharedRide.*
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserAlreadyExistsException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserLoginErrorException
import com.gitlab.uja2324dae.proyectodaekt.util.exceptions.user.UserNotFoundException
import com.gitlab.uja2324dae.proyectodaekt.util.normalize
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
class CarpoolingService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userRatingRepository: UserRatingRepository

    @Autowired
    private lateinit var sharedRideRepository: SharedRideRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    /**
     * Creates a new user.
     * @param user The user to be created.
     * @throws UserAlreadyExistsException If the user already exists.
     */
    fun newUser(@Valid user: User): User {
        if (userRepository.findByDni(user.dni) != null || userRepository.findByEmail(user.email) != null)
            throw UserAlreadyExistsException()

        // hash the password with bcrypt before saving
        user.password = passwordEncoder.encode(user.password)

        return userRepository.save(user)
    }

    /**
     * Creates a new shared ride.
     * @param driver The driver of the ride.
     * @param origin The origin city of the ride.
     * @param destination The destination city of the ride.
     * @param departureDateTime The departure date and time of the ride.
     * @param arrivalDateTime The arrival date and time of the ride.
     * @param availableSeats The number of available seats in the ride.
     * @param seatPrice The price per seat in the ride.
     * @throws InvalidSharedRideOriginOrDestinationException If the origin and destination are the same.
     * @throws InvalidSharedRidePriceException If the price is not between 1 and 100.
     * @throws InvalidSharedRideAvailableSeatsException If the number of available seats is not between 1 and 10.
     * @throws OverlappingSharedRideException If the ride overlaps with another ride of the same driver.
     */
    @Transactional
    fun newSharedRide(
        @Valid driver: User,
        origin: String,
        destination: String,
        departureDateTime: LocalDateTime,
        arrivalDateTime: LocalDateTime,
        availableSeats: Int,
        seatPrice: Double
    ): SharedRide {
        val loggedInDriver = userRepository.findByEmailAndPassword(driver.email, driver.password)
            ?: throw UserLoginErrorException()

        // A ride cannot have the same origin and destination
        if (origin.normalize() == destination.normalize())
            throw InvalidSharedRideOriginOrDestinationException()

        if (seatPrice < 1 || seatPrice > 100)
            throw InvalidSharedRidePriceException()

        if (availableSeats < 1 || availableSeats > 10)
            throw InvalidSharedRideAvailableSeatsException()

        val overlappingRide = sharedRideRepository.findOverlappingRides(
            driver.dni,
            departureDateTime,
            arrivalDateTime
        ).firstOrNull()

        if (overlappingRide != null)
            throw OverlappingSharedRideException()

        val newSharedRide = SharedRide(
            owner = driver,
            originCity = origin.normalize(),
            destinationCity = destination.normalize(),
            seats = availableSeats,
            departureTime = departureDateTime,
            arrivalTime = arrivalDateTime,
            seatPrice = seatPrice
        )
        newSharedRide.passengers.add(driver)
        loggedInDriver.sharedRides.add(newSharedRide)
        userRepository.save(loggedInDriver)
        return sharedRideRepository.save(newSharedRide)
    }

    /**
     * Logs in a user with the given email and password.
     * @param email The email of the user.
     * @param password The password of the user.
     * @return The user if the login was successful, null otherwise.
     */
    fun login(email: String, password: String): User? {
        val user = userRepository.findByEmail(email)

        if (user != null && passwordEncoder.matches(password, user.password)) {
            return user
        }
        return null
    }

    /**
     * Rates a user.
     * @param ratedBy The user who is rating.
     * @param targetUser The user who is being rated.
     * @param sharedRide The ride that the user is rating.
     * @param rating The rating, a number between 1 and 5.
     * @param message A short review of the user.
     * @return True if the rating was successful, false otherwise.
     */
    @Transactional
    fun rateUser(
        @Valid ratedBy: User,
        targetUserDni: String,
        sharedRideId: Long,
        rating: Int,
        message: String
    ) {
        val loggedInRatedBy = userRepository.findByEmailAndPassword(ratedBy.email, ratedBy.password)
            ?: throw UserLoginErrorException()

        val targetUser = userRepository.findByDni(targetUserDni) ?: throw UserNotFoundException()

        val sharedRide = sharedRideRepository.findById(sharedRideId).orElseThrow { SharedRideNotFoundException() }

        targetUser.addRating(loggedInRatedBy, sharedRide, rating, message)
        userRepository.save(targetUser)
    }

    /**
     * Find shared rides that match the given parameters. Starting from departureTime, until the end of departureTime's day.
     * @param originCity The origin city of the ride.
     * @param destinationCity The destination city of the ride.
     * @param departureTime The departure time of the ride.
     * @return A list of shared rides that match the given parameters.
     */
    fun findSharedRides(
        originCity: String,
        destinationCity: String,
        departureTime: LocalDateTime
    ): List<SharedRide>? {
        val departureTimeEnd = departureTime.withHour(23).withMinute(59)

        return sharedRideRepository.findByOriginCityAndDestinationCityAndDepartureTimeBetween(
            originCity.normalize(),
            destinationCity.normalize(),
            departureTime,
            departureTimeEnd
        )
    }

    /**
     * Finds user's pending acceptance ride requests.
     * @param user The user.
     * @return A list of shared rides that the user has requested and are pending acceptance.
     */
    fun getPendingAcceptanceSharedRides(@Valid user: User): List<SharedRide> {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        return sharedRideRepository.findByRequests_User_DniAndDepartureTimeGreaterThan(
            loggedInUser.dni,
            LocalDateTime.now()
        )
    }

    /**
     * Sends a request to join a ride.
     * @param user The user that wants to join the ride.
     * @param sharedRide The ride that the user wants to join.
     * @param message A message to the driver.
     */
    fun requestRide(@Valid user: User, @Valid sharedRide: SharedRide, message: String): SharedRide {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        sharedRide.requestRide(loggedInUser, message)
        sharedRideRepository.save(sharedRide)

        return sharedRide
    }

    /**
     * Accepts a user's request to join a ride.
     * @param user The user who sent the request that will be accepted.
     * @param sharedRide The ride that the user wants to join.
     */
    @Transactional
    fun acceptUserRequest(@Valid user: User, targetUserDni: String, sharedRideId: Long): SharedRide {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        val sharedRide = sharedRideRepository.findById(sharedRideId).orElseThrow { SharedRideNotFoundException() }

        val targetUser = userRepository.findByDni(targetUserDni) ?: throw UserNotFoundException()

        sharedRide.acceptUserRequest(loggedInUser, targetUser)
        sharedRideRepository.save(sharedRide)
        userRepository.save(targetUser)

        return sharedRide
    }

    /**
     * Denies a user's request to join a ride.
     * @param user The user who sent the request that will be denied.
     * @param sharedRide The ride that the user wants to join.
     */
    @Transactional
    fun denyUserRequest(@Valid user: User, targetUserDni: String, sharedRideId: Long): SharedRide {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        val sharedRide = sharedRideRepository.findById(sharedRideId).orElseThrow { SharedRideNotFoundException() }

        val targetUser = userRepository.findByDni(targetUserDni) ?: throw UserNotFoundException()

        sharedRide.denyUserRequest(loggedInUser, targetUser)
        sharedRideRepository.save(sharedRide)
        userRepository.save(targetUser)

        return sharedRide
    }

    /**
     * Finds the user's pending shared rides as a passenger.
     * @param user The user.
     * @return A list of shared rides that the user is accepted and are pending to be completed.
     */
    @Transactional
    fun getPendingSharedRidesAsPassenger(@Valid user: User): List<SharedRide> {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        return loggedInUser.pendingSharedRidesAsPassenger()
    }

    /**
     * Finds the user's pending shared rides as a driver.
     * @param user The user.
     * @return A list of shared rides that the user is the driver and are pending to be completed.
     */
    @Transactional
    fun getPendingSharedRidesAsDriver(@Valid user: User): List<SharedRide> {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        return loggedInUser.pendingSharedRidesAsDriver()
    }

    /**
     * Find a sharedRide by its id. Only allows passengers or drivers to find it.
     * @param user The user.
     * @param sharedRideId The id of the shared ride.
     * @return The shared ride if the user is a passenger or driver, null otherwise.
     * @throws SharedRideNotFoundException If the shared ride is not found.
     * @throws UserLoginErrorException If the user is not logged in.
     */
    fun getSharedRideById(@Valid user: User, sharedRideId: Long): SharedRide {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()

        val sharedRide = sharedRideRepository.findById(sharedRideId).orElseThrow { SharedRideNotFoundException() }

        return sharedRide
    }

    /**
     * Transactional method to obtain the ratings from a given user
     * @param user The user
     * @return A list of ratings
     */
    @Transactional
    fun getUserRatings(@Valid user: User): List<UserRating> {
        val loggedInUser = userRepository.findByEmailAndPassword(user.email, user.password)
            ?: throw UserLoginErrorException()
        loggedInUser.ratings.size

        return loggedInUser.ratings
    }

    /**
     * Method to obtain the user by its email
     * @param email The email of the user
     * @return The user
     */
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
}
