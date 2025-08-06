package com.example.neuerordner.data;

import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MutableLiveDataSet<T> extends MutableLiveData<Set<T>> {

    public MutableLiveDataSet() {
        super(Collections.unmodifiableSet(Collections.emptySet()));
    }



    public void setSingleValue(T value) {
        Set<T> current = getValue();
        Set<T> copy = new HashSet<T>(current);
        copy.add(value);
        super.setValue(copy);
    }

    @Override
    public void setValue(Set<T> value) {
        Set<T> current = getValue();
        Set<T> copy = new HashSet<>(current);
        copy.addAll(copy);
        super.setValue(copy);
    }

    @Override
    public Set<T> getValue() {
        return super.getValue();
    }

    public void deleteValue() {
        super.setValue(new HashSet<>());
    }
}

