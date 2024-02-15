package ru.infotecs.cicd;

public record KettleState(String id, ConnectedState connectedState, int temperature, SwitchMode switchMode) {
}
