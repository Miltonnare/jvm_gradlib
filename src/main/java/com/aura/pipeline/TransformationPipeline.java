package com.aura.pipeline;

import com.aura.internal.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes a sequence of stages on a pipeline input.
 */
public class TransformationPipeline<I, O> {
    private final List<Stage<?, ?>> stages = new ArrayList<>();

    public interface Stage<T, R> {
        R process(T input) throws Exception;
    }

    public static class PipelineException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public PipelineException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private TransformationPipeline(List<Stage<?, ?>> stages) {
        this.stages.addAll(stages);
    }

    public static <I, O> Builder<I, O> builder() {
        return new Builder<>();
    }

    @SuppressWarnings("unchecked")
    public O execute(I input) {
        Preconditions.checkNotNull(input, "Pipeline input cannot be null");
        Object current = input;
        for (Stage<?, ?> stage : stages) {
            try {
                current = ((Stage<Object, Object>) stage).process(current);
            } catch (Exception e) {
                throw new PipelineException("Pipeline execution failed at stage: " + stage.getClass().getSimpleName(), e);
            }
        }
        return (O) current;
    }

    public List<Stage<?, ?>> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public static class Builder<I, O> {
        private final List<Stage<?, ?>> stages = new ArrayList<>();

        public <R> Builder<I, R> addStage(Stage<O, R> stage) {
            Preconditions.checkNotNull(stage, "Stage cannot be null");
            stages.add(stage);
            return new Builder<>(stages);
        }

        // Internal constructor to carry over the stages with new types
        private Builder(List<Stage<?, ?>> stages) {
            this.stages.addAll(stages);
        }

        private Builder() {}

        @SuppressWarnings("unchecked")
        public TransformationPipeline<I, O> build() {
            return new TransformationPipeline<>((List<Stage<?, ?>>) (List<?>) stages);
        }
    }
}
