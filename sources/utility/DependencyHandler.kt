package com.github.fluidsonic.fluid.library

import org.gradle.api.artifacts.dsl.DependencyHandler


fun DependencyHandler.api(dependencyNotation: Any) =
	add("api", dependencyNotation)


@Suppress("unused")
fun DependencyHandler.fluid(name: String, version: String) =
	"com.github.fluidsonic:fluid-$name:$version"


@Suppress("unused")
fun DependencyHandler.kotlinx(name: String, version: String) =
	"org.jetbrains.kotlinx:kotlinx-$name:$version"