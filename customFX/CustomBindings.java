package de.julianpadawan.common.customFX;

import javafx.beans.binding.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CustomBindings {
    private CustomBindings() {
    }

    public static <P, R> ObjectBinding<R> ifNull(ObservableValue<P> observable,
                                                 Function<P, R> select, R ifNull) {
        return new ObjectBinding<>() {
            {
                bind(observable);
            }

            @Override
            protected R computeValue() {
                return observable.getValue() == null ? ifNull : select.apply(observable.getValue());
            }
        };
    }

    public static <P, R> ObjectExpression<R> apply(ObservableValue<P> observableValue, Function<P, R> select) {
        return new ObjectBinding<>() {
            {
                bind(observableValue);
            }

            @Override
            protected R computeValue() {
                return select.apply(observableValue.getValue());
            }
        };
    }

    public static <P, Q, R> ObjectExpression<R> apply(ObservableValue<P> observableValue, BiFunction<P, Q, R> select, Q param) {
        return new ObjectBinding<>() {
            {
                bind(observableValue);
            }

            @Override
            protected R computeValue() {
                return select.apply(observableValue.getValue(), param);
            }
        };
    }

    public static StringBinding join(ObservableStringValue first, String additional) {
        return new StringBinding() {
            {
                bind(first);
            }

            @Override
            protected String computeValue() {
                return first.get() + additional;
            }
        };
    }

    public static <P> IntegerExpression selectInt(ObservableValue<P> observableValue, Function<P, ObservableIntegerValue> select) {
        IntegerProperty property = new SimpleIntegerProperty();
        Runnable bindToCurrentValue = () ->
                Optional.ofNullable(observableValue.getValue()).map(select).ifPresentOrElse(property::bind, property::unbind);
        bindToCurrentValue.run();
        observableValue.addListener(observable -> bindToCurrentValue.run());
        return property;
    }

    public static <P, I, R> ObjectExpression<R> select(ObservableValue<P> observableValue,
                                                       Function<P, ObservableValue<I>> intermediate,
                                                       Function<I, ObservableValue<R>> select) {
        return select(select(observableValue, intermediate), select);
    }

    public static <P, R> ObjectExpression<R> select(ObservableValue<P> observableValue,
                                                    Function<P, ObservableValue<R>> select) {
        ObjectProperty<R> property = new SimpleObjectProperty<>();
        Runnable bindToCurrentValue = () ->
                Optional.ofNullable(observableValue.getValue()).map(select).ifPresentOrElse(property::bind, property::unbind);
        bindToCurrentValue.run();
        observableValue.addListener(observable -> bindToCurrentValue.run());
        return property;
    }

    public static BooleanExpression matches(TextField textField, String regex) {
        return new BooleanBinding() {
            {
                bind(textField.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return textField.getText().matches(regex);
            }
        };
    }

    public static <P, Q> BooleanExpression applyToBoolean(ObservableValue<P> observableValue, BiFunction<P, Q, Boolean> select, Q param) {
        return new BooleanBinding() {
            {
                bind(observableValue);
            }

            @Override
            protected boolean computeValue() {
                return observableValue.getValue() != null && select.apply(observableValue.getValue(), param);
            }
        };
    }
}
