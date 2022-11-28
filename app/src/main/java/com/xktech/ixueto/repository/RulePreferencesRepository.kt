package com.xktech.ixueto.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.xktech.ixueto.data.local.serializer.RuleSerializer
import com.xktech.ixueto.data.local.serializer.StudyRuleSerializer
import com.xktech.ixueto.datastore.Rule
import com.xktech.ixueto.datastore.StudyRule
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class RulePreferencesRepository @Inject constructor(
    private val context: Context,
) {
    private val Context.rulePreferencesStore: DataStore<Rule> by dataStore(
        fileName = "rule.pb",
        serializer = RuleSerializer
    )

    private val Context.studyRulePreferencesStore: DataStore<StudyRule> by dataStore(
        fileName = "studyRule.pb",
        serializer = StudyRuleSerializer
    )

    suspend fun getRule(): Rule {
        return context.rulePreferencesStore.data.first()
    }


    suspend fun setRule(rule: com.xktech.ixueto.model.Rule?): Boolean {
        if (rule != null) {
            context.rulePreferencesStore.updateData { currentRule ->
                currentRule.toBuilder()
                    .setStudyRule(context.studyRulePreferencesStore.updateData {
                        it.toBuilder()
                            .setIsCourseRegularStudy(rule.StudyRule.IsCourseRegularStudy)
                            .setSubjectStudyRule(rule.StudyRule.SubjectStudyRule.toInt())
                            .build()
                    })
                    .build()
            }
        } else {
            context.rulePreferencesStore.updateData { currentRule ->
                currentRule.toBuilder().clear().build()
            }
        }
        return true
    }
}