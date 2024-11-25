package com.example.freshlife;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class SettingsActivityUnitTest {

    private SettingsActivity settingsActivity;

    @Mock
    SharedPreferences sharedPreferences;

    @Mock
    SharedPreferences.Editor editor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Mock SharedPreferences behavior
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);

        settingsActivity = new SettingsActivity();
    }

    @Test
    public void testSaveNotificationSettings() {
        // Simulate user entering settings
//        int notificationDays = 5;
//        int notificationHour = 8;
//        int notificationMinute = 30;
//
//        // Simulate saving settings
//        settingsActivity.saveNotificationSettings(notificationDays, notificationHour, notificationMinute, sharedPreferences);
//
//        // Verify that the correct values were saved
//        verify(editor).putInt("notificationDays", notificationDays);
//        verify(editor).putInt("notificationHour", notificationHour);
//        verify(editor).putInt("notificationMinute", notificationMinute);
//        verify(editor).apply();
    }

    @Test
    public void testDarkModeToggle() {
        // Simulate toggling dark mode
//        boolean isDarkModeEnabled = true;
//
//        // Simulate saving dark mode preference
//        settingsActivity.saveDarkModePreference(isDarkModeEnabled, sharedPreferences);
//
//        // Verify that the preference was saved correctly
//        verify(editor).putBoolean("darkMode", isDarkModeEnabled);
//        verify(editor).apply();
    }
}
