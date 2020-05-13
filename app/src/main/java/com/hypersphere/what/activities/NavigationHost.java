package com.hypersphere.what.activities;

import androidx.fragment.app.Fragment;

/**
 * [from google codelabs 101-104]
 * A host (typically an {@code Activity}} that can display com.hypersphere.what.fragments and knows how to respond to
 * navigation events.
 */
public interface NavigationHost {
    /**
     * Trigger a navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this navigation reversible.
     */
    void navigateTo(Fragment fragment, boolean addToBackstack);
}
