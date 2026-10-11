-- SpendWise initial schema migration.
-- Fresh-database schema derived from the verified PostgreSQL schema dump.
-- Deliberately excludes the out-of-scope goals feature and unused PostgreSQL ENUM types.

--
-- PostgreSQL database dump
--

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

--
-- Name: budget_periods; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.budget_periods (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT budget_periods_check CHECK ((end_date >= start_date))
);

--
-- Name: budgets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.budgets (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    category_id uuid,
    budget_limit numeric(12,2) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    budget_period_id uuid NOT NULL,
    CONSTRAINT budgets_budget_limit_check CHECK ((budget_limit > (0)::numeric))
);

--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid,
    name character varying(50) NOT NULL,
    icon character varying(50),
    is_system boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_category_system CHECK ((((is_system = true) AND (user_id IS NULL)) OR ((is_system = false) AND (user_id IS NOT NULL))))
);

--
-- Name: email_verification_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.email_verification_tokens (
    id uuid NOT NULL,
    token_hash character varying(64) NOT NULL,
    user_id uuid NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: expense_shares; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.expense_shares (
    id uuid NOT NULL,
    expense_id uuid NOT NULL,
    user_id uuid NOT NULL,
    original_amount numeric(12,2) CONSTRAINT expense_shares_share_amount_not_null NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    remaining_amount numeric(12,2) NOT NULL,
    group_id uuid NOT NULL,
    percentage numeric(5,2),
    CONSTRAINT chk_expense_share_percentage CHECK (((percentage IS NULL) OR ((percentage > (0)::numeric) AND (percentage <= (100)::numeric)))),
    CONSTRAINT chk_expense_shares_remaining_amount CHECK (((remaining_amount >= (0)::numeric) AND (remaining_amount <= original_amount))),
    CONSTRAINT chk_expense_shares_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SETTLED'::character varying])::text[]))),
    CONSTRAINT expense_shares_share_amount_check CHECK ((original_amount > (0)::numeric))
);

--
-- Name: expenses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.expenses (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    category_id uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL,
    title character varying(100) NOT NULL,
    description character varying(255),
    amount numeric(12,2) NOT NULL,
    expense_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    CONSTRAINT expenses_amount_check CHECK ((amount > (0)::numeric)),
    CONSTRAINT expenses_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'VOIDED'::character varying])::text[])))
);

--
-- Name: group_members; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.group_members (
    group_id uuid NOT NULL,
    user_id uuid NOT NULL,
    joined_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_group_members_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'LEFT'::character varying])::text[])))
);

--
-- Name: groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.groups (
    id uuid NOT NULL,
    creator_id uuid NOT NULL,
    group_name character varying(50) NOT NULL,
    description character varying(255),
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_groups_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ARCHIVED'::character varying])::text[])))
);

--
-- Name: incomes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.incomes (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    source character varying(50) NOT NULL,
    description character varying(255),
    amount numeric(12,2) NOT NULL,
    income_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT incomes_amount_check CHECK ((amount > (0)::numeric))
);

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    recipient_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    title character varying(100) NOT NULL,
    message character varying(255) NOT NULL,
    reference_id uuid NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: settlements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.settlements (
    id uuid NOT NULL,
    expense_share_id uuid NOT NULL,
    payer_id uuid NOT NULL,
    receiver_id uuid NOT NULL,
    amount numeric(12,2) NOT NULL,
    settled_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status character varying(20) DEFAULT 'SETTLED'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    category_id uuid,
    wallet_id uuid NOT NULL,
    CONSTRAINT chk_payer_receiver CHECK ((payer_id <> receiver_id)),
    CONSTRAINT chk_settlements_status CHECK (((status)::text = 'SETTLED'::text)),
    CONSTRAINT settlements_amount_check CHECK ((amount > (0)::numeric))
);

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    username character varying(100) NOT NULL,
    name character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    phone_number character varying(20),
    password_hash character varying(255) NOT NULL,
    profile_picture_url character varying(255),
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    role character varying(20) DEFAULT 'USER'::character varying NOT NULL,
    email_verified boolean DEFAULT false NOT NULL
);

--
-- Name: wallets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallets (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    wallet_name character varying(100) NOT NULL,
    type character varying(20) NOT NULL,
    current_balance numeric(12,2) NOT NULL,
    status character varying(20) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_wallet_type CHECK (((type)::text = ANY ((ARRAY['CASH'::character varying, 'BANK'::character varying, 'CREDIT_CARD'::character varying, 'EWALLET'::character varying])::text[]))),
    CONSTRAINT wallets_current_balance_check CHECK ((current_balance >= (0)::numeric)),
    CONSTRAINT wallets_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ARCHIVED'::character varying])::text[])))
);

--
-- Name: budget_periods budget_periods_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budget_periods
    ADD CONSTRAINT budget_periods_pkey PRIMARY KEY (id);

--
-- Name: budgets budgets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_pkey PRIMARY KEY (id);

--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);

--
-- Name: email_verification_tokens email_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_pkey PRIMARY KEY (id);

--
-- Name: email_verification_tokens email_verification_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_token_hash_key UNIQUE (token_hash);

--
-- Name: expense_shares expense_shares_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_shares
    ADD CONSTRAINT expense_shares_pkey PRIMARY KEY (id);

--
-- Name: expenses expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_pkey PRIMARY KEY (id);

--
-- Name: group_members group_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id);

--
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);

--
-- Name: incomes incomes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incomes
    ADD CONSTRAINT incomes_pkey PRIMARY KEY (id);

--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);

--
-- Name: settlements settlements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT settlements_pkey PRIMARY KEY (id);

--
-- Name: expense_shares uq_expense_user; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_shares
    ADD CONSTRAINT uq_expense_user UNIQUE (expense_id, user_id);

--
-- Name: groups uq_group_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT uq_group_name UNIQUE (creator_id, group_name);

--
-- Name: wallets uq_wallet_user_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT uq_wallet_user_name UNIQUE (user_id, wallet_name);

--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

--
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);

--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (id);

--
-- Name: idx_budgets_period_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_budgets_period_category ON public.budgets USING btree (budget_period_id, category_id);

--
-- Name: idx_email_verification_token_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_email_verification_token_hash ON public.email_verification_tokens USING btree (token_hash);

--
-- Name: idx_email_verification_token_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_email_verification_token_user_id ON public.email_verification_tokens USING btree (user_id);

--
-- Name: idx_expense_shares_expense_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_expense_shares_expense_id ON public.expense_shares USING btree (expense_id);

--
-- Name: idx_expense_shares_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_expense_shares_user_id ON public.expense_shares USING btree (user_id);

--
-- Name: idx_expenses_user_expense_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_expenses_user_expense_at ON public.expenses USING btree (user_id, expense_at);

--
-- Name: idx_incomes_user_income_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_incomes_user_income_at ON public.incomes USING btree (user_id, income_at);

--
-- Name: idx_notifications_recipient_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_recipient_created_at ON public.notifications USING btree (recipient_id, created_at DESC);

--
-- Name: idx_notifications_recipient_is_read; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_recipient_is_read ON public.notifications USING btree (recipient_id, is_read);

--
-- Name: idx_settlements_payer_settled_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_settlements_payer_settled_at ON public.settlements USING btree (payer_id, settled_at);

--
-- Name: idx_settlements_receiver_settled_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_settlements_receiver_settled_at ON public.settlements USING btree (receiver_id, settled_at);

--
-- Name: uq_custom_category; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_custom_category ON public.categories USING btree (user_id, name) WHERE (user_id IS NOT NULL);

--
-- Name: uq_system_category; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_system_category ON public.categories USING btree (name) WHERE (user_id IS NULL);

--
-- Name: users users_updated_at_trigger; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER users_updated_at_trigger BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

--
-- Name: budgets fk_budget_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE RESTRICT;

--
-- Name: budgets fk_budget_period; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT fk_budget_period FOREIGN KEY (budget_period_id) REFERENCES public.budget_periods(id) ON DELETE CASCADE;

--
-- Name: budget_periods fk_budget_period_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budget_periods
    ADD CONSTRAINT fk_budget_period_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: budgets fk_budget_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: categories fk_category_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: email_verification_tokens fk_email_verification_token_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT fk_email_verification_token_user FOREIGN KEY (user_id) REFERENCES public.users(id);

--
-- Name: expenses fk_expense_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE RESTRICT;

--
-- Name: expense_shares fk_expense_share_group; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_shares
    ADD CONSTRAINT fk_expense_share_group FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE RESTRICT;

--
-- Name: expenses fk_expense_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: expenses fk_expense_wallet; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT fk_expense_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallets(id) ON DELETE RESTRICT;

--
-- Name: groups fk_group_creator; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT fk_group_creator FOREIGN KEY (creator_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: group_members fk_group_member_group; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE;

--
-- Name: group_members fk_group_member_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT fk_group_member_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT;

--
-- Name: incomes fk_income_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incomes
    ADD CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- Name: incomes fk_income_wallet; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.incomes
    ADD CONSTRAINT fk_income_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallets(id) ON DELETE RESTRICT;

--
-- Name: notifications fk_notifications_recipient; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES public.users(id);

--
-- Name: settlements fk_settlement_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT fk_settlement_category FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE RESTRICT;

--
-- Name: settlements fk_settlement_expense_share; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT fk_settlement_expense_share FOREIGN KEY (expense_share_id) REFERENCES public.expense_shares(id) ON DELETE RESTRICT;

--
-- Name: settlements fk_settlement_payer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT fk_settlement_payer FOREIGN KEY (payer_id) REFERENCES public.users(id) ON DELETE RESTRICT;

--
-- Name: settlements fk_settlement_receiver; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT fk_settlement_receiver FOREIGN KEY (receiver_id) REFERENCES public.users(id) ON DELETE RESTRICT;

--
-- Name: settlements fk_settlements_wallet; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.settlements
    ADD CONSTRAINT fk_settlements_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallets(id);

--
-- Name: expense_shares fk_share_expense; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_shares
    ADD CONSTRAINT fk_share_expense FOREIGN KEY (expense_id) REFERENCES public.expenses(id) ON DELETE CASCADE;

--
-- Name: expense_shares fk_share_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expense_shares
    ADD CONSTRAINT fk_share_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT;

--
-- Name: wallets fk_wallet_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
