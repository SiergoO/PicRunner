package com.picrunner.domain.usecase

interface UseCase<P : Param, R : Result<Any>> {

    suspend fun execute(param: P): R
}