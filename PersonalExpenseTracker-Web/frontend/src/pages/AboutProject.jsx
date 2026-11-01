import React from 'react';
import {
  Info,
  Layers,
  Database,
  Globe,
  Cpu,
  GraduationCap,
  ShieldCheck,
  Code2,
  CheckCircle,
} from 'lucide-react';

export const AboutProject = () => {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Header */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <h2 className="text-xl font-bold text-slate-900 tracking-tight">About Project & Academic Details</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Full-Stack Web Architecture, Technology Stack, and University Course Evaluation Guide
        </p>
      </div>

      {/* Project Overview */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-teal-50 text-teal-600 flex items-center justify-center">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-bold text-slate-900 text-base">Personal Expense Tracker — Web Edition</h3>
            <span className="text-xs text-teal-600 font-semibold">Spring Boot 3 + React 18 + PostgreSQL</span>
          </div>
        </div>

        <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
          The <b>Personal Expense Tracker Web Application</b> is an enterprise-grade full-stack personal finance SaaS product. Transformed from an individual Java Swing practical application, this platform modernizes financial management with stateless JWT authentication, reactive single-page architecture (React 18 & Tailwind CSS), transactional relational persistence (Spring Data JPA with Neon PostgreSQL / H2), and high-precision financial calculations using Java <code className="text-xs bg-slate-100 px-1 py-0.5 rounded text-slate-800">BigDecimal</code>.
        </p>
      </div>

      {/* Architecture Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Frontend Layer */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center gap-2 text-teal-600">
            <Globe className="w-5 h-5" />
            <h4 className="font-bold text-slate-900 text-sm">Frontend SPA</h4>
          </div>
          <p className="text-xs text-slate-500 leading-relaxed">
            Engineered with React.js, Vite, and Tailwind CSS. Features dynamic Recharts vector charts, token-aware REST client, responsive navigation sidebar, and instant UI state synchronization.
          </p>
          <div className="pt-2 border-t border-slate-100 text-[11px] font-mono text-slate-500">
            Target Deployment: <b>Vercel</b>
          </div>
        </div>

        {/* Backend Layer */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center gap-2 text-indigo-600">
            <Cpu className="w-5 h-5" />
            <h4 className="font-bold text-slate-900 text-sm">Spring Boot 3 REST API</h4>
          </div>
          <p className="text-xs text-slate-500 leading-relaxed">
            Multi-tier Java 21 architecture featuring Spring Web, Spring Security with stateless JWT Bearer validation, BCrypt password hashing, defensive validation, and centralized exception handling.
          </p>
          <div className="pt-2 border-t border-slate-100 text-[11px] font-mono text-slate-500">
            Target Deployment: <b>Render</b>
          </div>
        </div>

        {/* Database Layer */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
          <div className="flex items-center gap-2 text-emerald-600">
            <Database className="w-5 h-5" />
            <h4 className="font-bold text-slate-900 text-sm">Neon PostgreSQL</h4>
          </div>
          <p className="text-xs text-slate-500 leading-relaxed">
            Fully relational schema containing Users, Transactions, Categories, Budgets, and Settings. Enforces foreign key constraints, cascading renames, and complete multi-tenant user data isolation.
          </p>
          <div className="pt-2 border-t border-slate-100 text-[11px] font-mono text-slate-500">
            Database Provider: <b>Neon Cloud</b>
          </div>
        </div>
      </div>

      {/* Academic Student Credentials */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-4">
        <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
          <GraduationCap className="w-5 h-5 text-teal-600" />
          <h3 className="font-bold text-slate-900 text-sm">Academic Project Submission Details</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/70">
            <span className="text-slate-400 font-semibold block text-[10px] uppercase">Student Name</span>
            <span className="text-slate-800 font-bold text-sm">Ritika Kadian</span>
          </div>

          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/70">
            <span className="text-slate-400 font-semibold block text-[10px] uppercase">Roll Number / Enrollment</span>
            <span className="text-slate-800 font-bold text-sm">CS-2024-042</span>
          </div>

          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/70">
            <span className="text-slate-400 font-semibold block text-[10px] uppercase">Department</span>
            <span className="text-slate-800 font-bold text-sm">Department of Computer Science & Engineering</span>
          </div>

          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/70">
            <span className="text-slate-400 font-semibold block text-[10px] uppercase">Faculty / College</span>
            <span className="text-slate-800 font-bold text-sm">Faculty of Technology & Engineering</span>
          </div>
        </div>
      </div>

      {/* University Viva & Evaluation FAQ */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-4">
        <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
          <ShieldCheck className="w-5 h-5 text-teal-600" />
          <h3 className="font-bold text-slate-900 text-sm">Viva Voce Practical Evaluation Q&A</h3>
        </div>

        <div className="space-y-4 text-xs">
          <div>
            <h5 className="font-bold text-slate-800">Q1: How is user data isolated in the full-stack architecture?</h5>
            <p className="text-slate-600 mt-1 leading-relaxed">
              Every database entity (Transactions, Categories, Budgets, Settings) has a foreign key relationship to the <code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">User</code> table. Incoming REST API calls are filtered by the verified user ID extracted from the cryptographic JWT Bearer token, guaranteeing that users can only view and mutate their own financial data.
            </p>
          </div>

          <div>
            <h5 className="font-bold text-slate-800">Q2: How does the application prevent category deletion from corrupting existing transactions?</h5>
            <p className="text-slate-600 mt-1 leading-relaxed">
              In <code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">CategoryService.java</code>, before deleting a category, the system executes <code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">countByUserIdAndCategoryIgnoreCase()</code>. If any transactions reference that category, a <code className="bg-slate-100 px-1 py-0.5 rounded text-rose-700 font-mono">ValidationException</code> is thrown with an informative message, protecting data integrity.
            </p>
          </div>

          <div>
            <h5 className="font-bold text-slate-800">Q3: How are monetary calculations handled to prevent rounding drift?</h5>
            <p className="text-slate-600 mt-1 leading-relaxed">
              We strictly utilize Java <code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">BigDecimal</code> with <code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">RoundingMode.HALF_UP</code> across all services and persistence columns (<code className="bg-slate-100 px-1 py-0.5 rounded text-teal-700 font-mono">DECIMAL(14,2)</code>). This eliminates IEEE-754 floating-point inaccuracies typical with <code className="bg-slate-100 px-1 py-0.5 rounded text-slate-800 font-mono">float</code> or <code className="bg-slate-100 px-1 py-0.5 rounded text-slate-800 font-mono">double</code>.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
