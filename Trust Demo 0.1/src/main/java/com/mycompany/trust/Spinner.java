package com.mycompany.trust;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

/**
 * Minimal generic Spinner implementation for integer values, compatible with TrustSystem usage.
 * For real-world use, prefer javafx.scene.control.Spinner, but this class is provided for custom/compatibility needs.
 */
public class Spinner<T extends Number> extends Control {
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();
    private final T min;
    private final T max;
    private final T initialValue;
    private final ObservableList<T> items;

    public Spinner(int min, int max, int initialValue) {
        this.min = (T) Integer.valueOf(min);
        this.max = (T) Integer.valueOf(max);
        this.initialValue = (T) Integer.valueOf(initialValue);
        this.value.set(this.initialValue);
        this.items = FXCollections.observableArrayList();
        for (int i = min; i <= max; i++) {
            this.items.add((T) Integer.valueOf(i));
        }
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T newValue) {
        if (newValue.doubleValue() < min.doubleValue() || newValue.doubleValue() > max.doubleValue()) return;
        value.set(newValue);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public T getInitialValue() {
        return initialValue;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        // For a real implementation, return a custom skin or delegate to JavaFX Spinner
        return null;
    }
}
