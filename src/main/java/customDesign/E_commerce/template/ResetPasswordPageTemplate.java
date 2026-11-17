
package customDesign.E_commerce.template;

public final class ResetPasswordPageTemplate {

    private ResetPasswordPageTemplate() {
    }

    public static final String NAME = "reset_password";

    public static final String SUBJECT = "Reset Password Page";

    public static final String HTML = """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="referrer" content="no-referrer">
                <title>Reset password</title>

                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background: #f4f6f8;
                        margin: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                    }

                    .card {
                        background: #fff;
                        padding: 32px;
                        border-radius: 10px;
                        width: 340px;
                        box-shadow: 0 4px 16px rgba(0,0,0,.1);
                    }

                    h2 {
                        margin-top: 0;
                    }

                    input {
                        width: 100%;
                        padding: 10px;
                        margin: 8px 0 14px;
                        box-sizing: border-box;
                        border: 1px solid #ccc;
                        border-radius: 6px;
                    }

                    button {
                        width: 100%;
                        padding: 11px;
                        border: 0;
                        border-radius: 6px;
                        background: #2563eb;
                        color: #fff;
                        font-size: 15px;
                        cursor: pointer;
                    }

                    button:disabled {
                        background: #93b4f5;
                        cursor: not-allowed;
                    }

                    #msg {
                        margin-top: 14px;
                        font-size: 14px;
                    }

                    .ok {
                        color: #15803d;
                    }

                    .err {
                        color: #b91c1c;
                    }
                </style>
            </head>

            <body>
                <div class="card">
                    <h2>Reset your password</h2>

                    <form id="f">
                        <label>New password</label>
                        <input id="pw" type="password"
                               minlength="6" required autocomplete="new-password">

                        <label>Confirm password</label>
                        <input id="pw2" type="password"
                               minlength="6" required autocomplete="new-password">

                        <button id="btn" type="submit">
                            Reset password
                        </button>
                    </form>

                    <div id="msg" role="status" aria-live="polite"></div>
                </div>

                <script>
                    const token =
                        new URLSearchParams(location.search).get('token');

                    const msg = document.getElementById('msg');
                    const btn = document.getElementById('btn');
                    const form = document.getElementById('f');

                    if (!token) {
                        msg.className = 'err';
                        msg.textContent =
                            'Invalid link: token is missing.';
                        btn.disabled = true;
                    }

                    form.addEventListener('submit', async (e) => {
                        e.preventDefault();

                        if (!token) return;

                        const pw = document.getElementById('pw').value;
                        const pw2 = document.getElementById('pw2').value;

                        if (pw !== pw2) {
                            msg.className = 'err';
                            msg.textContent = 'Passwords do not match.';
                            return;
                        }

                        btn.disabled = true;
                        msg.className = '';
                        msg.textContent = 'Please wait...';

                        try {
                            const res = await fetch(
                                '/api/auth/reset-password',
                                {
                                    method: 'POST',
                                    headers: {
                                        'Content-Type': 'application/json'
                                    },
                                    body: JSON.stringify({
                                        token: token,
                                        newPassword: pw
                                    })
                                }
                            );

                            let body = {};

                            try {
                                body = await res.json();
                            } catch (_) {
                            }

                            if (res.ok) {
                                msg.className = 'ok';
                                msg.textContent =
                                    (body.data ||
                                    'Password reset successfully') +
                                    '. You can now log in.';

                                form.style.display = 'none';
                            } else {
                                msg.className = 'err';
                                msg.textContent =
                                    body.data ||
                                    'Invalid or expired link. Please request a new one.';

                                btn.disabled = false;
                            }

                        } catch (err) {
                            msg.className = 'err';
                            msg.textContent =
                                'Could not reach the server.';

                            btn.disabled = false;
                        }
                    });
                </script>
            </body>
            </html>
            """;
}
