package com.himanshoe.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Custom rule that treats ARGB-style color literals (e.g. 0xFF2196F3) used in Color(...) calls
 * as non-magic numbers. This is intended to complement the standard MagicNumber rule.
 */
class ColorMagicNumberRule(
    config: Config = Config.empty,
) : Rule(config) {
    override val issue: Issue =
        Issue(
            id = "ColorMagicNumber",
            severity = Severity.Style,
            description = "Allows ARGB color hex literals in Color(...) calls to be treated as non-magic numbers.",
            debt = Debt.FIVE_MINS,
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val calleeName = expression.calleeExpression?.text ?: return
        if (calleeName != "Color") return

        expression.valueArguments
            .mapNotNull(KtValueArgument::getArgumentExpression)
            .filterIsInstance<KtConstantExpression>()
            .forEach { constant ->
                val text = constant.text
                // If this is an ARGB-style hex literal, we simply don't consider it a problem here.
                if (text.startsWith("0xFF") || text.startsWith("0XFF")) {
                    // No report: this rule's purpose is to explicitly allow these.
                    return@forEach
                }
            }
    }
}

class CustomRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "custom-rules"

    override fun instance(config: Config): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(ColorMagicNumberRule(config)),
        )
}
