package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class TemporalValidatorTest :
    FunSpec({

        context("LocalDate") {
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDate.validate() = localDate(clock) { future() }

                test("success") {
                    shouldBeValid { date.plusDays(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { date.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { date.minusDays(1).validate() }
                }
            }

            context("futureOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDate.validate() = localDate(clock) { futureOrPresent() }

                test("success - future") {
                    shouldBeValid { date.plusDays(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { date.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { date.minusDays(1).validate() }
                }
            }

            context("past") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDate.validate() = localDate(clock) { past() }

                test("success") {
                    shouldBeValid { date.minusDays(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { date.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { date.plusDays(1).validate() }
                }
            }

            context("pastOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val zone = ZoneOffset.UTC
                val instant = date.atStartOfDay(zone).toInstant()
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDate.validate() = localDate(clock) { pastOrPresent() }

                test("success - past") {
                    shouldBeValid { date.minusDays(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { date.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { date.plusDays(1).validate() }
                }
            }

            context("min") {
                val minDate = LocalDate.of(2025, 1, 1)

                test("success - equal") {
                    shouldBeValid { minDate min minDate }
                }

                test("success - greater") {
                    shouldBeValid { minDate.plusDays(1) min minDate }
                }

                test("failure") {
                    shouldBeInvalid { minDate.minusDays(1) min minDate }
                }
            }

            context("max") {
                val maxDate = LocalDate.of(2025, 12, 31)

                test("success - equal") {
                    shouldBeValid { maxDate max maxDate }
                }

                test("success - less") {
                    shouldBeValid { maxDate.minusDays(1) max maxDate }
                }

                test("failure") {
                    shouldBeInvalid { maxDate.plusDays(1) max maxDate }
                }
            }

            context("gt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    shouldBeValid { date.plusDays(1) gt date }
                }

                test("failure - equal") {
                    shouldBeInvalid { date gt date }
                }

                test("failure - less") {
                    shouldBeInvalid { date.minusDays(1) gt date }
                }
            }

            context("gte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success - greater") {
                    shouldBeValid { date.plusDays(1) gte date }
                }

                test("success - equal") {
                    shouldBeValid { date gte date }
                }

                test("failure") {
                    shouldBeInvalid { date.minusDays(1) gte date }
                }
            }

            context("lt") {
                val date = LocalDate.of(2025, 6, 15)

                test("success") {
                    shouldBeValid { date.minusDays(1) lt date }
                }

                test("failure - equal") {
                    shouldBeInvalid { date lt date }
                }

                test("failure - greater") {
                    shouldBeInvalid { date.plusDays(1) lt date }
                }
            }

            context("lte") {
                val date = LocalDate.of(2025, 6, 15)

                test("success - less") {
                    shouldBeValid { date.minusDays(1) lte date }
                }

                test("success - equal") {
                    shouldBeValid { date lte date }
                }

                test("failure") {
                    shouldBeInvalid { date.plusDays(1) lte date }
                }
            }
        }

        context("LocalTime") {
            context("future") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalTime.validate() = localTime(clock) { future() }

                test("success") {
                    shouldBeValid { time.plusHours(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { time.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { time.minusHours(1).validate() }
                }
            }

            context("futureOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalTime.validate() = localTime(clock) { futureOrPresent() }

                test("success - future") {
                    shouldBeValid { time.plusHours(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { time.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { time.minusHours(1).validate() }
                }
            }

            context("past") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalTime.validate() = localTime(clock) { past() }

                test("success") {
                    shouldBeValid { time.minusHours(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { time.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { time.plusHours(1).validate() }
                }
            }

            context("pastOrPresent") {
                val date = LocalDate.of(2025, 1, 1)
                val time = LocalTime.of(12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = date.atTime(time).toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalTime.validate() = localTime(clock) { pastOrPresent() }

                test("success - past") {
                    shouldBeValid { time.minusHours(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { time.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { time.plusHours(1).validate() }
                }
            }

            context("min") {
                val minTime = LocalTime.of(9, 0, 0)

                test("success - equal") {
                    shouldBeValid { minTime min minTime }
                }

                test("success - greater") {
                    shouldBeValid { minTime.plusHours(1) min minTime }
                }

                test("failure") {
                    shouldBeInvalid { minTime.minusHours(1) min minTime }
                }
            }

            context("max") {
                val maxTime = LocalTime.of(17, 0, 0)

                test("success - equal") {
                    shouldBeValid { maxTime max maxTime }
                }

                test("success - less") {
                    shouldBeValid { maxTime.minusHours(1) max maxTime }
                }

                test("failure") {
                    shouldBeInvalid { maxTime.plusHours(1) max maxTime }
                }
            }

            context("gt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    shouldBeValid { time.plusHours(1) gt time }
                }

                test("failure - equal") {
                    shouldBeInvalid { time gt time }
                }

                test("failure - less") {
                    shouldBeInvalid { time.minusHours(1) gt time }
                }
            }

            context("gte") {
                val time = LocalTime.of(12, 0, 0)

                test("success - greater") {
                    shouldBeValid { time.plusHours(1) gte time }
                }

                test("success - equal") {
                    shouldBeValid { time gte time }
                }

                test("failure") {
                    shouldBeInvalid { time.minusHours(1) gte time }
                }
            }

            context("lt") {
                val time = LocalTime.of(12, 0, 0)

                test("success") {
                    shouldBeValid { time.minusHours(1) lt time }
                }

                test("failure - equal") {
                    shouldBeInvalid { time lt time }
                }

                test("failure - greater") {
                    shouldBeInvalid { time.plusHours(1) lt time }
                }
            }

            context("lte") {
                val time = LocalTime.of(12, 0, 0)

                test("success - less") {
                    shouldBeValid { time.minusHours(1) lte time }
                }

                test("success - equal") {
                    shouldBeValid { time lte time }
                }

                test("failure") {
                    shouldBeInvalid { time.plusHours(1) lte time }
                }
            }
        }

        context("LocalDateTime") {
            context("future") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDateTime.validate() = localDateTime(clock) { future() }

                test("success") {
                    shouldBeValid { dateTime.plusHours(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { dateTime.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { dateTime.minusHours(1).validate() }
                }
            }

            context("futureOrPresent") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDateTime.validate() = localDateTime(clock) { futureOrPresent() }

                test("success - future") {
                    shouldBeValid { dateTime.plusHours(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { dateTime.validate() }
                }

                test("failure - past") {
                    shouldBeInvalid { dateTime.minusHours(1).validate() }
                }
            }

            context("past") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDateTime.validate() = localDateTime(clock) { past() }

                test("success") {
                    shouldBeValid { dateTime.minusHours(1).validate() }
                }

                test("failure - present") {
                    shouldBeInvalid { dateTime.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { dateTime.plusHours(1).validate() }
                }
            }

            context("pastOrPresent") {
                val dateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
                val zone = ZoneOffset.UTC
                val instant = dateTime.toInstant(zone)
                val clock = Clock.fixed(instant, zone)

                context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
                fun LocalDateTime.validate() = localDateTime(clock) { pastOrPresent() }

                test("success - past") {
                    shouldBeValid { dateTime.minusHours(1).validate() }
                }

                test("success - present") {
                    shouldBeValid { dateTime.validate() }
                }

                test("failure - future") {
                    shouldBeInvalid { dateTime.plusHours(1).validate() }
                }
            }

            context("min") {
                val minDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)

                test("success - equal") {
                    shouldBeValid { minDateTime min minDateTime }
                }

                test("success - greater") {
                    shouldBeValid { minDateTime.plusHours(1) min minDateTime }
                }

                test("failure") {
                    shouldBeInvalid { minDateTime.minusHours(1) min minDateTime }
                }
            }

            context("max") {
                val maxDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                test("success - equal") {
                    shouldBeValid { maxDateTime max maxDateTime }
                }

                test("success - less") {
                    shouldBeValid { maxDateTime.minusHours(1) max maxDateTime }
                }

                test("failure") {
                    shouldBeInvalid { maxDateTime.plusHours(1) max maxDateTime }
                }
            }

            context("gt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    shouldBeValid { dateTime.plusHours(1) gt dateTime }
                }

                test("failure - equal") {
                    shouldBeInvalid { dateTime gt dateTime }
                }

                test("failure - less") {
                    shouldBeInvalid { dateTime.minusHours(1) gt dateTime }
                }
            }

            context("gte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success - greater") {
                    shouldBeValid { dateTime.plusHours(1) gte dateTime }
                }

                test("success - equal") {
                    shouldBeValid { dateTime gte dateTime }
                }

                test("failure") {
                    shouldBeInvalid { dateTime.minusHours(1) gte dateTime }
                }
            }

            context("lt") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success") {
                    shouldBeValid { dateTime.minusHours(1) lt dateTime }
                }

                test("failure - equal") {
                    shouldBeInvalid { dateTime lt dateTime }
                }

                test("failure - greater") {
                    shouldBeInvalid { dateTime.plusHours(1) lt dateTime }
                }
            }

            context("lte") {
                val dateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0)

                test("success - less") {
                    shouldBeValid { dateTime.minusHours(1) lte dateTime }
                }

                test("success - equal") {
                    shouldBeValid { dateTime lte dateTime }
                }

                test("failure") {
                    shouldBeInvalid { dateTime.plusHours(1) lte dateTime }
                }
            }
        }
    })
