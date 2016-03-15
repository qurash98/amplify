/**
 * Copyright 2015 Stuart Kent
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.amplify.prompt;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.stkent.amplify.R;
import com.github.stkent.amplify.prompt.interfaces.IPromptPresenter;
import com.github.stkent.amplify.prompt.interfaces.IPromptView;
import com.github.stkent.amplify.prompt.interfaces.IQuestionPresenter;
import com.github.stkent.amplify.prompt.interfaces.IQuestionView;
import com.github.stkent.amplify.prompt.interfaces.IThanksView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.stkent.amplify.prompt.interfaces.IPromptPresenter.UserOpinion.NEGATIVE;
import static com.github.stkent.amplify.prompt.interfaces.IPromptPresenter.UserOpinion.POSITIVE;

abstract class BasePromptView<T extends View & IQuestionView, U extends View & IThanksView>
        extends FrameLayout implements IPromptView {

    @NonNull
    protected abstract T getQuestionView();

    @NonNull
    protected abstract U getThanksView();

    private T displayedQuestionView;

    protected boolean isDisplayed;

    private final IQuestionPresenter userOpinionQuestionPresenter =
            new IQuestionPresenter() {
                @Override
                public void userRespondedPositively() {
                    promptPresenter.setUserOpinion(POSITIVE);
                }

                @Override
                public void userRespondedNegatively() {
                    promptPresenter.setUserOpinion(NEGATIVE);
                }
            };

    private final IQuestionPresenter feedbackQuestionPresenter =
            new IQuestionPresenter() {
                @Override
                public void userRespondedPositively() {
                    promptPresenter.userAgreedToGiveFeedback();
                }

                @Override
                public void userRespondedNegatively() {
                    promptPresenter.userDeclinedToGiveFeedback();
                }
            };

    private IPromptPresenter promptPresenter;
    private BasePromptViewConfig basePromptViewConfig;

    public BasePromptView(final Context context) {
        this(context, null);
    }

    public BasePromptView(final Context context, @Nullable final AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BasePromptView(
            final Context context,
            @Nullable final AttributeSet attributeSet,
            final int defStyleAttr) {

        super(context, attributeSet, defStyleAttr);
        setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        initializeBaseConfig(attributeSet);
    }

    @Override
    public final void setPresenter(@NonNull final IPromptPresenter promptPresenter) {
        this.promptPresenter = promptPresenter;
    }

    @Override
    public final void queryUserOpinion() {
        final T userOpinionQuestionView = getQuestionView();
        userOpinionQuestionView.setPresenter(userOpinionQuestionPresenter);
        userOpinionQuestionView.bind(basePromptViewConfig.getUserOpinionQuestion());

        setContentView(userOpinionQuestionView);

        displayedQuestionView = userOpinionQuestionView;

        isDisplayed = true;
    }

    @Override
    public final void requestPositiveFeedback() {
        displayedQuestionView.setPresenter(feedbackQuestionPresenter);
        displayedQuestionView.bind(basePromptViewConfig.getPositiveFeedbackQuestion());
    }

    @Override
    public final void requestCriticalFeedback() {
        final T criticalFeedbackQuestionView = getQuestionView();
        criticalFeedbackQuestionView.setPresenter(feedbackQuestionPresenter);
        criticalFeedbackQuestionView.bind(basePromptViewConfig.getCriticalFeedbackQuestion());

        setContentView(criticalFeedbackQuestionView);
    }

    @Override
    public final void thankUser() {
        final U thanksView = getThanksView();
        thanksView.bind(basePromptViewConfig.getThanks());

        setContentView(thanksView);

        promptPresenter = null;
    }

    @Override
    public final void dismiss() {
        setVisibility(GONE);
        promptPresenter = null;
    }

    /**
     * Note: <code>Theme.obtainStyledAttributes</code> accepts a null <code>AttributeSet</code>; see
     * documentation of that method for confirmation.
     */
    private void initializeBaseConfig(@Nullable final AttributeSet attributeSet) {
        final TypedArray typedArray = getContext().getTheme()
                .obtainStyledAttributes(attributeSet, R.styleable.BasePromptView, 0, 0);

        basePromptViewConfig = new BasePromptViewConfig(typedArray);

        typedArray.recycle();
    }

    private void setContentView(@NonNull final View view) {
        removeAllViews();
        addView(view, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

}
