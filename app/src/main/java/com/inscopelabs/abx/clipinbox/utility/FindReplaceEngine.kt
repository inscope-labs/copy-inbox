package com.inscopelabs.abx.clipinbox.utility

import java.util.regex.PatternSyntaxException

object FindReplaceEngine {
    fun replace(input: String, find: String, replacement: String, useRegex: Boolean): Result<String> {
        if (find.isBlank()) {
            return Result.failure(IllegalArgumentException("empty search text"))
        }
        return if (useRegex) {
            try {
                val regex = Regex(find)
                Result.success(regex.replace(input, replacement))
            } catch (e: PatternSyntaxException) {
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            }
        } else {
            Result.success(input.replace(find, replacement))
        }
    }
}
