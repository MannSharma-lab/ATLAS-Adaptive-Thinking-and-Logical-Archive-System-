package com.agon.app.utils

import com.agon.app.data.CategoryEntity

object CategorizationHelper {
    
    private val shoppingKeywords = listOf("₹", "$", "price", "buy", "offer", "discount", "cart", "order", "amazon", "flipkart")
    private val paymentsKeywords = listOf("account", "ifsc", "upi", "bank", "transaction", "paid", "received", "balance", "debited", "credited", "gpay", "paytm", "phonepe")
    private val studyKeywords = listOf("notes", "lecture", "formula", "pdf", "syllabus", "exam", "assignment", "question", "answer", "definition")
    private val socialKeywords = listOf("tweet", "retweets", "likes", "instagram", "facebook", "post", "comment", "share", "profile")

    fun categorize(extractedText: String, categories: List<CategoryEntity>): String {
        val textLower = extractedText.lowercase()
        
        // 1. Check User Created Folders First (Higher Priority)
        val userCategories = categories.filter { it.isUserCreated }
        for (category in userCategories) {
            val intentKeywords = generateKeywordsFromFolderName(category.displayName)
            if (intentKeywords.any { textLower.contains(it) }) {
                return category.id
            }
        }
        
        // 2. Check Default System Folders
        if (shoppingKeywords.any { textLower.contains(it) }) {
            return categories.find { it.systemKey == "shopping" }?.id ?: "others"
        }
        
        if (paymentsKeywords.any { textLower.contains(it) }) {
            return categories.find { it.systemKey == "payments" }?.id ?: "others"
        }
        
        if (studyKeywords.any { textLower.contains(it) }) {
            return categories.find { it.systemKey == "study" }?.id ?: "others"
        }
        
        if (socialKeywords.any { textLower.contains(it) }) {
            return categories.find { it.systemKey == "social" }?.id ?: "others"
        }
        
        // Default fallback
        return categories.find { it.systemKey == "others" }?.id ?: "others"
    }
    
    private fun generateKeywordsFromFolderName(folderName: String): List<String> {
        val nameLower = folderName.lowercase()
        val baseKeywords = mutableListOf(nameLower)
        
        // Rule-based expansion based on folder name intent
        when {
            nameLower.contains("animal") || nameLower.contains("pet") -> {
                baseKeywords.addAll(listOf("dog", "cat", "lion", "tiger", "wildlife", "zoo", "bird", "fish", "puppy", "kitten"))
            }
            nameLower.contains("computer") || nameLower.contains("tech") || nameLower.contains("code") -> {
                baseKeywords.addAll(listOf("command", "programming", "terminal", "error", "cpu", "ram", "software", "hardware", "bug", "developer", "java", "python"))
            }
            nameLower.contains("food") || nameLower.contains("recipe") -> {
                baseKeywords.addAll(listOf("cook", "kitchen", "meal", "dinner", "lunch", "breakfast", "restaurant", "delicious", "ingredients"))
            }
            nameLower.contains("travel") || nameLower.contains("trip") -> {
                baseKeywords.addAll(listOf("flight", "hotel", "booking", "ticket", "vacation", "tour", "destination", "passport"))
            }
            nameLower.contains("health") || nameLower.contains("fitness") -> {
                baseKeywords.addAll(listOf("workout", "gym", "diet", "doctor", "hospital", "medicine", "prescription", "calories"))
            }
        }
        
        return baseKeywords
    }
}
