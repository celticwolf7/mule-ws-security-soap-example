/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.wandrell.example.mule.wss.security.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.OpenSAMLUtil;
import org.apache.ws.security.validate.Credential;
import org.apache.ws.security.validate.SamlAssertionValidator;

/**
 * Validator for SAML messages.
 * <p>
 * It makes sure the message is configured correctly.
 * 
 * @author Bernardo Martínez Garrido
 */
public final class SamlCustomValidator extends SamlAssertionValidator {

    /**
     * The subject name.
     */
    private final String subjectName;
    /**
     * The issuer name.
     */
    private final String issuerName;

    /**
     * Default constructor.
     * 
     * @param name
     *            the subject name
     * @param issuer
     *            the issuer name
     */
    public SamlCustomValidator(final String name,
            final String issuer) {
        super();

        subjectName = checkNotNull(name, "Received a null pointer as the subject name");
        issuerName = checkNotNull(issuer, "Received a null pointer as the issuer name");
    }

    /**
     * Returns the issuer name.
     * @return the issuer name
     */
    public final String getIssuerName() {
        return issuerName;
    }

    @Override
    public final Credential validate(final Credential credential,
            final RequestData data) throws WSSecurityException {
        final Credential returnedCredential; // Credential to work with

        checkNotNull(credential, "Received a null pointer as credential");
        checkNotNull(data, "Received a null pointer as data");

        returnedCredential = super.validate(credential, data);

        // Reject not self issued credentials
        AssertionWrapper assertion = credential.getAssertion();
        if (!getIssuerName().equals(assertion.getIssuerString())) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLsecurity");
        }

        // Reject if it doesn't support SAML 2.0
        if (assertion.getSaml2() == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLsecurity");
        }

        // Reject if there is no confirmation method
        String confirmationMethod = assertion.getConfirmationMethods().get(0);
        if (confirmationMethod == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLsecurity");
        }
        if (!OpenSAMLUtil.isMethodSenderVouches(confirmationMethod)) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLsecurity");
        }

        if (!getSubjectName().equals(
                assertion.getSaml2().getSubject().getNameID().getValue())) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidSAMLsecurity");
        }

        return returnedCredential;
    }

    /**
     * Returns the name of the subject.
     * 
     * @return the name of the subject
     */
    private final String getSubjectName() {
        return subjectName;
    }

}
