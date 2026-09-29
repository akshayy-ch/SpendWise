import { useEffect, useState } from "react";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { Check, MailCheck } from "lucide-react";
import { authApi } from "../../api/authApi";
import { AuthLayout } from "./LoginPage";

export default function VerifyEmailPage() {
  const [params] = useSearchParams();
  const location = useLocation();
  const [state, setState] = useState<"loading"|"success"|"error">("loading");
  const [message, setMessage] = useState("");
  const [email, setEmail] = useState((location.state as {email?:string} | null)?.email ?? "");

  useEffect(() => {
    const token = params.get("token");
    if (!token) { setState("error"); setMessage("Open the verification link from your SpendWise email."); return; }
    authApi.verifyEmail(token).then((result) => {
      setState("success"); setMessage(result);
    }).catch((err) => {
      setState("error"); setMessage(err.response?.data?.message ?? "This verification link is invalid or expired.");
    });
  }, [params]);

  return <AuthLayout eyebrow={state === "success" ? "Email verified" : "Almost there"} title={state === "success" ? "You're all set." : "Check your inbox."} subtitle={state === "success" ? "Your SpendWise account is ready. You can sign in now." : "Use the verification link sent to your email address to activate your account."}>
    <div className={`verification-state ${state}`}><div className="verification-icon">{state === "success" ? <Check size={28}/> : <MailCheck size={28}/>}</div><strong>{state === "loading" ? "Verifying your email..." : state === "success" ? "Email verified successfully" : "Verification needs attention"}</strong><p>{message}</p>{email && state !== "success" && <span>Sent to <b>{email}</b></span>}</div>
    <Link to="/login" className="button button-primary auth-submit">Continue to sign in</Link>
  </AuthLayout>;
}
