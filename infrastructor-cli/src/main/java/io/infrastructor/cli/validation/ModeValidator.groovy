package io.infrastructor.cli.validation

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.ParameterException

public class ModeValidator implements IParameterValidator {
    
    def static final FULL = 'FULL'
    def static final PART = 'PART'
    
    @Override
    void validate(String name, String value) throws ParameterException {
        if (!(value?.toUpperCase() in [FULL, PART])) {
            throw new ParameterException("parameter '$name' should be '$FULL' or '$PART'")
        }
    }	
}

