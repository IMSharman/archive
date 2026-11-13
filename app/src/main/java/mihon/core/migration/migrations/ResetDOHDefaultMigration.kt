package mihon.core.migration.migrations

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.network.PREF_DOH_CLOUDFLARE
import eu.kanade.tachiyomi.network.PREF_DOH_DISABLED
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

class ResetDOHDefaultMigration : Migration {
    override val version = 138f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val networkPreferences = migrationContext.get<NetworkPreferences>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val currentDoh = prefs.getInt(networkPreferences.dohProvider().key(), PREF_DOH_DISABLED)
        if (currentDoh == PREF_DOH_CLOUDFLARE) {
            prefs.edit {
                putInt(networkPreferences.dohProvider().key(), PREF_DOH_DISABLED)
            }
        }

        return true
    }
}
