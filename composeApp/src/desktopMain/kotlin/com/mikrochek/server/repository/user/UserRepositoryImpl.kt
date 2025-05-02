//package com.mikrochek.server.repository.user
//
//import com.mikrochek.server.database.Collection
//import com.mikrochek.server.database.models.User
//import com.mikrochek.config.EnvVariables
//import com.mongodb.client.model.Filters
//import org.bson.types.ObjectId
//import org.litote.kmongo.coroutine.CoroutineClient
//
//class UserRepositoryImpl(
//    private val client: CoroutineClient
//) : UserRepository {
//    private val db = client.getDatabase(EnvVariables.ORGANIZATION_ID)
//    private val userCollection = db.getCollection<User>(Collection.USER)
//
//    override suspend fun createUser(user: User): User {
//        userCollection.insertOne(user)
//        return user
//    }
//
//    override suspend fun getUserById(id: ObjectId): User? {
//        val filter = Filters.eq(User::_id.name, id)
//        return userCollection.find(filter).first()
//    }
//
//    override suspend fun getAllUsers(): List<User> {
//        return userCollection.find().toList()
//    }
//
//    override suspend fun getUserByUserName(userName: String): User? {
//        val filter = Filters.eq(User::userName.name, userName)
//        return userCollection.find(filter).first()
//    }
//}