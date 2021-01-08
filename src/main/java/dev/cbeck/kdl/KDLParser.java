package dev.cbeck.kdl;

import dev.cbeck.kdl.antlr.kdlLexer;
import dev.cbeck.kdl.antlr.kdlParser;
import dev.cbeck.kdl.objects.KDLDocument;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class KDLParser {

    private static final String ex =
            "// Based on https://github.com/NuGet/NuGet.Client/blob/dev/src/NuGet.Clients/NuGet.CommandLine/NuGet.CommandLine.csproj\n" +
                    "Project {\n" +
                    "  PropertyGroup {\n" +
                    "    IsCommandLinePackage true\n" +
                    "  }\n" +
                    "\n" +
                    "  Import Project=r\"$([MSBuild]::GetDirectoryNameOfFileAbove($(MSBuildThisFileDirectory), 'README.md'))\\build\\common.props\"\n" +
                    "  Import Project=\"Sdk.props\" Sdk=\"Microsoft.NET.Sdk\"\n" +
                    "  Import Project=\"ilmerge.props\"\n" +
                    "\n" +
                    "  PropertyGroup {\n" +
                    "    RootNamespace \"NuGet.CommandLine\"\n" +
                    "    AssemblyName \"NuGet\"\n" +
                    "    AssemblyTitle \"NuGet Command Line\"\n" +
                    "    PackageId \"NuGet.CommandLine\"\n" +
                    "    TargetFramework \"$(NETFXTargetFramework)\"\n" +
                    "    GenerateDocumentationFile false\n" +
                    "    Description \"NuGet Command Line Interface.\"\n" +
                    "    ApplicationManifest \"app.manifest\"\n" +
                    "    Shipping true\n" +
                    "    OutputType \"Exe\"\n" +
                    "    ComVisible false\n" +
                    "    // Pack properties\n" +
                    "    PackProject true\n" +
                    "    IncludeBuildOutput false\n" +
                    "    TargetsForTfmSpecificContentInPackage \"$(TargetsForTfmSpecificContentInPackage)\" \"CreateCommandlineNupkg\"\n" +
                    "    SuppressDependenciesWhenPacking true\n" +
                    "    DevelopmentDependency true\n" +
                    "    PackageRequireLicenseAcceptance false\n" +
                    "    UsePublicApiAnalyzer false\n" +
                    "  }\n" +
                    "\n" +
                    "  Target Name=\"CreateCommandlineNupkg\" {\n" +
                    "    ItemGroup {\n" +
                    "      TfmSpecificPackageFile Include=r\"$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.exe\" {\n" +
                    "        PackagePath \"tools/\"\n" +
                    "      }\n" +
                    "      TfmSpecificPackageFile Include=r\"$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.pdb\" {\n" +
                    "        PackagePath \"tools/\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup Condition=\"$(DefineConstants.Contains(SIGNED_BUILD))\" {\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.FuncTest, PublicKey=002400000480000094000000060200000024000052534131000400000100010007d1fa57c4aed9f0a32e84aa0faefd0de9e8fd6aec8f87fb03766c834c99921eb23be79ad9d5dcc1dd9ad236132102900b723cf980957fc4e177108fc607774f29e8320e92ea05ece4e821c0a5efe8f1645c4c0c93c1ab99285d622caa652c1dfad63d745d6f2de5f17e5eaf0fc4963d261c8a12436518206dc093344d5ad293\"\n" +
                    "    }\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.Test, PublicKey=002400000480000094000000060200000024000052534131000400000100010007d1fa57c4aed9f0a32e84aa0faefd0de9e8fd6aec8f87fb03766c834c99921eb23be79ad9d5dcc1dd9ad236132102900b723cf980957fc4e177108fc607774f29e8320e92ea05ece4e821c0a5efe8f1645c4c0c93c1ab99285d622caa652c1dfad63d745d6f2de5f17e5eaf0fc4963d261c8a12436518206dc093344d5ad293\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup Condition=\"!$(DefineConstants.Contains(SIGNED_BUILD))\" {\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.FuncTest\"\n" +
                    "    }\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.Test\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup Condition=\"$(DefineConstants.Contains(SIGNED_BUILD))\" {\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.Test, PublicKey=002400000480000094000000060200000024000052534131000400000100010007d1fa57c4aed9f0a32e84aa0faefd0de9e8fd6aec8f87fb03766c834c99921eb23be79ad9d5dcc1dd9ad236132102900b723cf980957fc4e177108fc607774f29e8320e92ea05ece4e821c0a5efe8f1645c4c0c93c1ab99285d622caa652c1dfad63d745d6f2de5f17e5eaf0fc4963d261c8a12436518206dc093344d5ad293\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup Condition=\"!$(DefineConstants.Contains(SIGNED_BUILD))\" {\n" +
                    "    AssemblyAttribute Include=\"System.Runtime.CompilerServices.InternalsVisibleTo\" {\n" +
                    "      _Parameter1 \"NuGet.CommandLine.Test\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup {\n" +
                    "    Reference Include=\"Microsoft.Build.Utilities.v4.0\"\n" +
                    "    Reference Include=\"Microsoft.CSharp\"\n" +
                    "    Reference Include=\"System\"\n" +
                    "    Reference Include=\"System.ComponentModel.Composition\"\n" +
                    "    Reference Include=\"System.ComponentModel.Composition.Registration\"\n" +
                    "    Reference Include=\"System.ComponentModel.DataAnnotations\"\n" +
                    "    Reference Include=\"System.IO.Compression\"\n" +
                    "    Reference Include=\"System.Net.Http\"\n" +
                    "    Reference Include=\"System.Xml\"\n" +
                    "    Reference Include=\"System.Xml.Linq\"\n" +
                    "    Reference Include=\"NuGet.Core\" {\n" +
                    "      HintPath r\"$(SolutionPackagesFolder)nuget.core\\2.14.0-rtm-832\\lib\\net40-Client\\NuGet.Core.dll\"\n" +
                    "      Aliases \"CoreV2\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "  ItemGroup {\n" +
                    "    PackageReference Include=\"Microsoft.VisualStudio.Setup.Configuration.Interop\"\n" +
                    "    ProjectReference Include=r\"$(NuGetCoreSrcDirectory)NuGet.PackageManagement\\NuGet.PackageManagement.csproj\"\n" +
                    "    ProjectReference Include=r\"$(NuGetCoreSrcDirectory)NuGet.Build.Tasks\\NuGet.Build.Tasks.csproj\"\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup {\n" +
                    "    EmbeddedResource Update=\"NuGetCommand.resx\" {\n" +
                    "      Generator \"ResXFileCodeGenerator\"\n" +
                    "      LastGenOutput \"NuGetCommand.Designer.cs\"\n" +
                    "    }\n" +
                    "    Compile Update=\"NuGetCommand.Designer.cs\" {\n" +
                    "      DesignTime true\n" +
                    "      AutoGen true\n" +
                    "      DependentUpon \"NuGetCommand.resx\"\n" +
                    "    }\n" +
                    "    EmbeddedResource Update=\"NuGetResources.resx\" {\n" +
                    "      // Strings are shared by other projects, use public strings.\n" +
                    "      Generator \"PublicResXFileCodeGenerator\"\n" +
                    "      LastGenOutput \"NuGetResources.Designer.cs\"\n" +
                    "    }\n" +
                    "    Compile Update=\"NuGetResources.Designer.cs\" {\n" +
                    "      DesignTime true\n" +
                    "      AutoGen true\n" +
                    "      DependentUpon \"NuGetResources.resx\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  ItemGroup {\n" +
                    "    EmbeddedResource Include=r\"$(NuGetCoreSrcDirectory)NuGet.Build.Tasks\\NuGet.targets\" {\n" +
                    "      Link \"NuGet.targets\"\n" +
                    "      SubType \"Designer\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  // Since we are moving some code and strings from NuGet.CommandLine to NuGet.Commands, we opted to go through normal localization process (build .resources.dll) and then add them to the ILMerged nuget.exe\n" +
                    "  // This will also be called from CI build, after assemblies are localized, since our test infra takes nuget.exe before Localization\n" +
                    "  Target Name=\"ILMergeNuGetExe\" \\\n" +
                    "         AfterTargets=\"Build\" \\\n" +
                    "         Condition=\"'$(BuildingInsideVisualStudio)' != 'true' and '$(SkipILMergeOfNuGetExe)' != 'true'\" \\\n" +
                    "  {\n" +
                    "    PropertyGroup {\n" +
                    "      // when done after build, no localizedartifacts are built yet, so expected localized artifact count is 0.\n" +
                    "      ExpectedLocalizedArtifactCount 0 Condition=\"'$(ExpectedLocalizedArtifactCount)' == ''\"\n" +
                    "    }\n" +
                    "    ItemGroup {\n" +
                    "      BuildArtifacts Include=r\"$(OutputPath)\\*.dll\" Exclude=\"@(MergeExclude)\"\n" +
                    "      // NuGet.exe needs all NuGet.Commands.resources.dll merged in\n" +
                    "      LocalizedArtifacts Include=r\"$(ArtifactsDirectory)\\NuGet.Commands\\**\\$(NETFXTargetFramework)\\**\\*.resources.dll\"\n" +
                    "    }\n" +
                    "    Error Text=\"Build dependencies are inconsistent with mergeinclude specified in ilmerge.props\" \\\n" +
                    "          Condition=\"'@(BuildArtifacts-&gt;Count())' != '@(MergeInclude-&gt;Count())'\"\n" +
                    "    Error Text=\"Satellite assemblies count ILMerged into NuGet.exe should be $(ExpectedLocalizedArtifactCount), but was: @(LocalizedArtifacts-&gt;Count())\" \\\n" +
                    "          Condition=\"'@(LocalizedArtifacts-&gt;Count())' != '$(ExpectedLocalizedArtifactCount)'\"\n" +
                    "    PropertyGroup {\n" +
                    "      PathToBuiltNuGetExe \"$(OutputPath)NuGet.exe\"\n" +
                    "      IlmergeCommand r\"$(ILMergeExePath) /lib:$(OutputPath) /out:$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.exe @(MergeAllowDup -> '/allowdup:%(Identity)', ' ') /log:$(OutputPath)IlMergeLog.txt\"\n" +
                    "      IlmergeCommand Condition=\"Exists($(MS_PFX_PATH))\" \"$(IlmergeCommand) /delaysign /keyfile:$(MS_PFX_PATH)\"\n" +
                    "      // LocalizedArtifacts need fullpath, since there will be duplicate file names\n" +
                    "      IlmergeCommand \"$(IlmergeCommand) $(PathToBuiltNuGetExe) @(BuildArtifacts->'%(filename)%(extension)', ' ') @(LocalizedArtifacts->'%(fullpath)', ' ')\"\n" +
                    "    }\n" +
                    "    MakeDir Directories=\"$(ArtifactsDirectory)$(VsixOutputDirName)\"\n" +
                    "    Exec Command=\"$(IlmergeCommand)\" ContinueOnError=\"false\"\n" +
                    "  }\n" +
                    "\n" +
                    "  Import Project=\"$(BuildCommonDirectory)common.targets\"\n" +
                    "  Import Project=\"$(BuildCommonDirectory)embedinterop.targets\"\n" +
                    "\n" +
                    "  // Do nothing. This basically strips away the framework assemblies from the resulting nuspec.\n" +
                    "  Target Name=\"_GetFrameworkAssemblyReferences\" DependsOnTargets=\"ResolveReferences\"\n" +
                    "\n" +
                    "  Target Name=\"GetSigningInputs\" Returns=\"@(DllsToSign)\" {\n" +
                    "    ItemGroup {\n" +
                    "      DllsToSign Include=r\"$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.exe\" {\n" +
                    "        StrongName \"MsSharedLib72\"\n" +
                    "        Authenticode \"Microsoft400\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  Target Name=\"GetSymbolsToIndex\" Returns=\"@(SymbolsToIndex)\" {\n" +
                    "    ItemGroup {\n" +
                    "      SymbolsToIndex Include=r\"$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.exe\"\n" +
                    "      SymbolsToIndex Include=r\"$(ArtifactsDirectory)$(VsixOutputDirName)\\NuGet.pdb\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "\n" +
                    "  Import Project=\"Sdk.targets\" Sdk=\"Microsoft.NET.Sdk\"\n" +
                    "}";

    public static void main(String[] args) throws IOException {
        final KDLParser parser = new KDLParser();
        final KDLDocument document = parser.parse(new StringReader(ex));
        
        System.out.println(document.toKDLPretty(4));
    }

    public KDLDocument parse(Reader reader) throws IOException {
        final kdlLexer lexer = new kdlLexer(CharStreams.fromReader(reader));
        final kdlParser parser = new kdlParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());

        final KDLVisitorImpl visitor = new KDLVisitorImpl();
        return (KDLDocument) visitor.visit(parser.parse());
    }
}
