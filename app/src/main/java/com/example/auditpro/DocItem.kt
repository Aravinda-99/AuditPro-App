package com.example.auditpro

import java.time.LocalDate
import java.time.LocalTime

data class DocItem(
    var name: String,
    var description: String,
    var section: String,
    var company: String,
    var requestDate: LocalDate?,
    var requestTime: LocalTime? // Newly added field for time
)
