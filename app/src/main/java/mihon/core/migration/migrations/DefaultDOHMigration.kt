package mihon.core.migration.migrations

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.network.PREF_DOH_CLOUDFLARE
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

class DefaultDOHMigration : Migration {
    override val version = 134f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val networkPreferences = migrationContext.get<NetworkPreferences>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val currentDoh = prefs.getInt(networkPreferences.dohProvider().key(), -1)
        if (currentDoh == -1) {
            prefs.edit {
                putInt(networkPreferences.dohProvider().key(), PREF_DOH_CLOUDFLARE)
            }
        }

        return true
    }
}
