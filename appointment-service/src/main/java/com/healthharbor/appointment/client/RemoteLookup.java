package com.healthharbor.appointment.client;

import com.healthharbor.common.exception.BusinessException;
import feign.FeignException;
import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

/** Translates downstream Feign failures into meaningful business errors. */
public final class RemoteLookup {
    private RemoteLookup() {
    }

    public static <T> T fetch(String what, Object id, Supplier<T> call) {
        try {
            return call.get();
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(what + " " + id + " does not exist");
        } catch (FeignException ex) {
            throw new BusinessException("Unable to verify " + what.toLowerCase() + " right now (" + ex.status() + ")",
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (RuntimeException ex) {
            throw new BusinessException("Unable to verify " + what.toLowerCase() + ": dependent service unavailable",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
